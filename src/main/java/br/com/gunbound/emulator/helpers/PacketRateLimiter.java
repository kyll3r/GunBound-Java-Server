package br.com.gunbound.emulator.helpers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;

/**
 * Handler que limita a taxa de pacotes recebidos por conexão. Protege contra
 * flood attacks e spam de pacotes.
 */
public class PacketRateLimiter extends ChannelInboundHandlerAdapter {

	private static final Logger logger = LoggerFactory.getLogger(PacketRateLimiter.class);

	// Atributos do canal para armazenar contadores
	private static final AttributeKey<AtomicInteger> PACKET_COUNT = AttributeKey.valueOf("packet_rate_count");
	private static final AttributeKey<AtomicLong> WINDOW_START = AttributeKey.valueOf("packet_rate_window_start");
	private static final AttributeKey<AtomicInteger> VIOLATION_COUNT = AttributeKey.valueOf("packet_rate_violations");

	private final int maxPacketsPerWindow;
	private final long windowDurationMillis;
	private final boolean closeOnViolation;
	private final int maxViolationsBeforeClose;

	/**
	 * Construtor com configuração padrão.
	 * 
	 * @param maxPackets Número máximo de pacotes permitidos
	 * @param timeUnit   Unidade de tempo da janela
	 */
	public PacketRateLimiter(int maxPackets, TimeUnit timeUnit) {
		this(maxPackets, 1, timeUnit, false, 5);
	}

	/**
	 * Construtor completo.
	 * 
	 * @param maxPackets               Número máximo de pacotes permitidos
	 * @param windowDuration           Duração da janela de tempo
	 * @param timeUnit                 Unidade de tempo
	 * @param closeOnViolation         Se true, fecha conexão imediatamente em
	 *                                 violação
	 * @param maxViolationsBeforeClose Número de violações antes de fechar (se
	 *                                 closeOnViolation=false)
	 */
	public PacketRateLimiter(int maxPackets, long windowDuration, TimeUnit timeUnit, boolean closeOnViolation,
			int maxViolationsBeforeClose) {
		if (maxPackets <= 0) {
			throw new IllegalArgumentException("maxPackets deve ser maior que zero");
		}
		if (windowDuration <= 0) {
			throw new IllegalArgumentException("windowDuration deve ser maior que zero");
		}

		this.maxPacketsPerWindow = maxPackets;
		this.windowDurationMillis = timeUnit.toMillis(windowDuration);
		this.closeOnViolation = closeOnViolation;
		this.maxViolationsBeforeClose = maxViolationsBeforeClose;

		logger.info("PacketRateLimiter inicializado: maxPackets={}, window={}ms, closeOnViolation={}, maxViolations={}",
				maxPackets, windowDurationMillis, closeOnViolation, maxViolationsBeforeClose);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// Inicializa contadores para este canal
		ctx.channel().attr(PACKET_COUNT).set(new AtomicInteger(0));
		ctx.channel().attr(WINDOW_START).set(new AtomicLong(System.currentTimeMillis()));
		ctx.channel().attr(VIOLATION_COUNT).set(new AtomicInteger(0));

		ctx.fireChannelActive();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		long currentTime = System.currentTimeMillis();

		AtomicInteger packetCount = ctx.channel().attr(PACKET_COUNT).get();
		AtomicLong windowStart = ctx.channel().attr(WINDOW_START).get();
		AtomicInteger violationCount = ctx.channel().attr(VIOLATION_COUNT).get();

		// Verifica se a janela de tempo expirou
		long windowAge = currentTime - windowStart.get();
		if (windowAge >= windowDurationMillis) {
			// Reseta a janela
			windowStart.set(currentTime);
			packetCount.set(0);

			logger.debug("Janela de rate limit resetada para {}", ctx.channel().remoteAddress());
		}

		// Incrementa contador de pacotes
		int currentCount = packetCount.incrementAndGet();

		// Verifica se excedeu o limite
		if (currentCount > maxPacketsPerWindow) {
			int violations = violationCount.incrementAndGet();

			logger.warn("Rate limit excedido: {}/{} pacotes em {}ms. Violações: {}. Cliente: {}", currentCount,
					maxPacketsPerWindow, windowDurationMillis, violations, ctx.channel().remoteAddress());

			// Descarta o pacote (não propaga)
			if (msg instanceof io.netty.buffer.ByteBuf) {
				((io.netty.buffer.ByteBuf) msg).release();
			}

			// Decide se fecha a conexão
			if (closeOnViolation || violations >= maxViolationsBeforeClose) {
				logger.warn("Fechando conexão por excesso de rate limit: {} violações. Cliente: {}", violations,
						ctx.channel().remoteAddress());
				ctx.close();
			}

			return; // Não propaga o pacote
		}

		// Pacote dentro do limite, propaga
		ctx.fireChannelRead(msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("Exceção no PacketRateLimiter: {}", ctx.channel().remoteAddress(), cause);
		ctx.fireExceptionCaught(cause);
	}
}