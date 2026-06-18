package br.com.gunbound.emulator.helpers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handler que limita o tamanho máximo de pacotes recebidos.
 * Protege contra ataques de buffer overflow e pacotes malformados.
 */
public class PacketSizeLimiter extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(PacketSizeLimiter.class);

    private final int maxPacketSize;
    private final boolean closeOnViolation;

    /**
     * Construtor com configuração padrão (fecha conexão em violação).
     * 
     * @param maxPacketSize Tamanho máximo permitido em bytes
     */
    public PacketSizeLimiter(int maxPacketSize) {
        this(maxPacketSize, true);
    }

    /**
     * Construtor completo.
     * 
     * @param maxPacketSize Tamanho máximo permitido em bytes
     * @param closeOnViolation Se true, fecha a conexão ao detectar violação
     */
    public PacketSizeLimiter(int maxPacketSize, boolean closeOnViolation) {
        if (maxPacketSize <= 0) {
            throw new IllegalArgumentException("maxPacketSize deve ser maior que zero");
        }
        this.maxPacketSize = maxPacketSize;
        this.closeOnViolation = closeOnViolation;

        logger.info("PacketSizeLimiter inicializado: maxSize={}KB, closeOnViolation={}", 
                   maxPacketSize / 1024, closeOnViolation);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!(msg instanceof ByteBuf)) {
            ctx.fireChannelRead(msg);
            return;
        }

        ByteBuf buf = (ByteBuf) msg;
        int packetSize = buf.readableBytes();

        if (packetSize > maxPacketSize) {
            logger.warn("Pacote excede tamanho máximo: {}KB > {}KB. Cliente: {}", 
                       packetSize / 1024, 
                       maxPacketSize / 1024, 
                       ctx.channel().remoteAddress());

            // Libera o buffer para evitar memory leak
            buf.release();

            if (closeOnViolation) {
                logger.warn("Fechando conexão por violação de tamanho: {}", 
                           ctx.channel().remoteAddress());
                ctx.close();
            }

            return; // Não propaga o pacote
        }

        // Pacote válido, propaga para o próximo handler
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exceção no PacketSizeLimiter: {}", ctx.channel().remoteAddress(), cause);
        ctx.fireExceptionCaught(cause);
    }
}