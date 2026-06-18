package br.com.gunbound.emulator.services.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import br.com.gunbound.emulator.ServerConfig;
import br.com.gunbound.emulator.gameserver.packets.readers.GoldUpdateReader;
import br.com.gunbound.emulator.model.DAO.DAOFactory;
import br.com.gunbound.emulator.model.DAO.UserDAO;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.services.HonkService;
import br.com.gunbound.emulator.services.excepion.InsufficientFundsException;
import br.com.gunbound.emulator.utils.Utils;

public class HonkServiceImpl implements HonkService {

	private final UserDAO userDAO;
	private final ServerConfig serverConfig;
	private final ReceiptServiceImpl log;

	public HonkServiceImpl() {
		this.userDAO = DAOFactory.CreateUserDao();
		this.serverConfig = ServerConfig.getInstance();
		this.log = new ReceiptServiceImpl();

	}

	@Override
	public void processPurchaseHonk(PlayerSession player, String msg) throws InsufficientFundsException {
		int price = (serverConfig.getGameServerHonkPrice() != null) ? serverConfig.getGameServerHonkPrice() : 5000;

		// validação
		if (player.getGold() < price) {
			throw new InsufficientFundsException(
					"Saldo de Gold insuficiente. Necessário: " + price + ", Possui: " + player.getGold());
		}

		// primeiro banco, depois memória
		userDAO.updateMinusGold(player.getUserNameId(), price);

		// sincroniza sessão
		player.setGold(player.getGold() - price);

		if (player.getCurrentLobby() != null) {
			GoldUpdateReader.goldUpdateWriter(player.getPlayerCtx());
		}
		
		log.insertReceiptLogHonk(player, msg);

	}

	@Override
	public void processPurchaseAlterColors(PlayerSession player, int colorId, boolean IsBalloon)
			throws InsufficientFundsException {

		int priceColor = (serverConfig.getGameServerColorPrice() != null) ? serverConfig.getGameServerColorPrice()
				: 50000;
		int priceBalloon = (serverConfig.getGameServerBalloonPrice() != null) ? serverConfig.getGameServerBalloonPrice()
				: 50000;

		// verifica se comprando ou atualizand o balao ou a fala
		int price = (IsBalloon) ? priceBalloon : priceColor;
		LocalDateTime expiration;

		if (IsBalloon) {
			expiration = Utils.convertToLocalDateTime(player.getCorGameBalaoTime());
		} else {
			expiration = Utils.convertToLocalDateTime(player.getCorGameTime());
		}

		if (expiration.isBefore(LocalDateTime.now())) {

			// validação
			if (player.getGold() < price) {
				throw new InsufficientFundsException(
						"Saldo de Gold insuficiente. Necessário: " + price + ", Possui: " + player.getGold());
			}

			// primeiro banco, depois memória
			userDAO.updateMinusGold(player.getUserNameId(), price);

			// sincroniza sessão
			player.setGold(player.getGold() - price);
			
			Timestamp timeExpire = Timestamp.valueOf(LocalDateTime.now().plusDays(30));
			userDAO.updateColors(player.getUserNameId(), colorId, timeExpire,IsBalloon);
			
			if(IsBalloon) {
				player.setCorGameBalaoTime(timeExpire);
				log.insertReceiptLogGeneral(player, "CLRL-BAL", "Purchased and updated the balloon color:" + colorId);
			}else {
				player.setCorGameTime(timeExpire);
				log.insertReceiptLogGeneral(player, "CLR-TEXT", "Purchased and updated the text color: " + colorId);
			}

			if (player.getCurrentLobby() != null) {
				GoldUpdateReader.goldUpdateWriter(player.getPlayerCtx());
			}

		}else {
			//isBalloon ja foi setado la em cima.
			userDAO.updateColors(player.getUserNameId(), colorId, Timestamp.valueOf(expiration),IsBalloon);
			log.insertReceiptLogGeneral(player, ( IsBalloon ? "CLR-BAL" : "CLR-TEXT"), "Updated the " + ( IsBalloon ? "Balloon" : "Text") +" Color: "+ colorId);
			
		}

	}

}