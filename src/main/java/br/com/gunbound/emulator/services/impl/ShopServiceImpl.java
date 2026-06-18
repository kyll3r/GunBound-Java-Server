package br.com.gunbound.emulator.services.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.com.gunbound.emulator.ServerConfig;
import br.com.gunbound.emulator.model.DAO.ChestDAO;
import br.com.gunbound.emulator.model.DAO.DAOFactory;
import br.com.gunbound.emulator.model.DAO.UserDAO;
import br.com.gunbound.emulator.model.entities.DTO.ChestDTO;
import br.com.gunbound.emulator.model.entities.DTO.MenuDTO;
import br.com.gunbound.emulator.model.entities.DTO.receipts.ReceiptBuyDTO;
import br.com.gunbound.emulator.model.entities.DTO.receipts.ReceiptGiftDTO;
import br.com.gunbound.emulator.model.entities.DTO.receipts.ReceiptSellDTO;
import br.com.gunbound.emulator.model.entities.game.GameMenu;
import br.com.gunbound.emulator.model.entities.game.PlayerAvatar;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.services.ReceiptService;
import br.com.gunbound.emulator.services.ShopService;
import br.com.gunbound.emulator.services.excepion.ShopException;
import br.com.gunbound.emulator.utils.Utils;

public class ShopServiceImpl implements ShopService {

	private final UserDAO userDAO;
	private final ChestDAO chestDAO;
	private final GameMenu gameMenu;
	private final ServerConfig serverConfig;
	private final ReceiptService receiptService;

	public ShopServiceImpl() {
		this.userDAO = DAOFactory.CreateUserDao();
		this.chestDAO = DAOFactory.CreateChestDao();
		this.gameMenu = GameMenu.getInstance();
		this.serverConfig = ServerConfig.getInstance();
		this.receiptService = new ReceiptServiceImpl();
	}

	@Override
	public List<ChestDTO> getPlayerItems(PlayerSession player) {

		//lista vinda do banco
		List<ChestDTO> playerItemsRaw = chestDAO.getAllAvatarsByOwnerId(player.getUserNameId());
		//lista ja tratada
		List<ChestDTO> playerItemsFinal = new ArrayList<ChestDTO>();

		Integer puItem = serverConfig.getGameServerSuperUserItem();
		puItem = puItem != null ? puItem : 204801;

		// valida se é um PowerUser
		int isPowerUser = Utils.hasActivePowerUserItem(playerItemsRaw);
		player.setIsPowerUser(isPowerUser);

		playerItemsRaw.forEach(avatar -> {
			// Verifica se a data de expiração do avatar é nula ou se é uma data futura
			boolean isValid = avatar.getExpire() == null
					|| Utils.convertToLocalDateTime(avatar.getExpire()).isAfter(LocalDateTime.now());

			if (isValid) {
				// Adiciona o avatar se ele estiver dentro da validade (ou for eterno)
				// player.getPlayerAvatars().add(new PlayerAvatar(avatar));
				playerItemsFinal.add(avatar);
			}else {
				chestDAO.disableAvatar(avatar.getIdx(), player.getUserNameId());
			}
		});

		// return chestDAO.getAllAvatarsByOwnerId(player.getUserNameId());
		return playerItemsFinal;
	}

	@Override
	public ChestDTO buyAvatar(PlayerSession player, int avatarCode, int currencyType) throws ShopException {
		// 1. Valida se o item existe na loja
		MenuDTO avatarData = gameMenu.getByNo(avatarCode);
		if (avatarData == null) {
			throw new ShopException("Item com código " + avatarCode + " não encontrado na loja.");
		}

		// 2. Verifica o saldo do jogador
		int price = (currencyType == 0) ? avatarData.getPriceByGoldForI() : avatarData.getPriceByCashForI();
		if (currencyType == 0 && player.getGold() < price) {
			throw new ShopException(
					"Saldo de Gold insuficiente. Necessário: " + price + ", Possui: " + player.getGold());
		}
		if (currencyType == 1 && player.getCash() < price) {
			throw new ShopException(
					"Saldo de Cash insuficiente. Necessário: " + price + ", Possui: " + player.getCash());
		}

		// 3. Prepara o novo item para ser inserido no iventário
		ChestDTO avatarToInsert = prepareNewAvatar(player, avatarCode, avatarData);
		avatarToInsert.setAcquisition(currencyType == 0 ? "G" : "C");

		try {
			// 4. Executa a transação no banco de dados
			if (currencyType == 0) {
				userDAO.updateMinusGold(player.getUserNameId(), price);
			} else {
				userDAO.updateMinusCash(player.getUserNameId(), price);
			}
			Integer newAvatarIdx = chestDAO.insert(avatarToInsert);

			// 5. Atualiza o saldo na sessão do jogador em memória
			if (currencyType == 0) {
				player.setGold(player.getGold() - price);
			} else {
				player.setCash(player.getCash() - price);
			}

			// 6. Retorna o item recém-criado para confirmação
			ChestDTO newAvatarInChest = chestDAO.getByIdx(newAvatarIdx);
			newAvatarInChest.setItem(avatarCode); // Ajuste para o "hack" do Power User

			// 7. Insere o log de compra
			receiptService.insertReceiptBuy(new ReceiptBuyDTO(player, newAvatarInChest, currencyType, price));

			System.out.println(
					"Item '" + avatarData.getMenuName() + "' comprado com sucesso por " + player.getNickName());
			return newAvatarInChest;

		} catch (Exception e) {
			e.printStackTrace();
			// Em um sistema real, aqui ocorreria um rollback da transação.
			throw new ShopException("Ocorreu um erro no banco de dados durante a compra.");
		}
	}

	/**
	 * Método auxiliar para construir o objeto ChestDTO com as regras de "negócio"
	 * para PU.
	 */
	private ChestDTO prepareNewAvatar(PlayerSession player, int originalAvatarCode, MenuDTO avatarData) {
		Integer puItem = serverConfig.getGameServerSuperUserItem();
		puItem = puItem != null ? puItem : 204801;

		ChestDTO avatar = new ChestDTO();
		avatar.setWearing("0");
		avatar.setVolume(avatarData.getVolume1());
		avatar.setRecovered("0");
		avatar.setOwnerId(player.getUserNameId());
		avatar.setExpireType("I");

		// Lógica para o place order
		PlayerAvatar highestAvatar = player.getAvatarWithHighestPlaceOrder();
		String highestPlaceOrder = (highestAvatar != null) ? highestAvatar.getPlaceOrder() : "0";
		String nextPlaceOrder = Integer.toString((Integer.parseInt(highestPlaceOrder) + 10000));
		avatar.setPlaceOrder(nextPlaceOrder);

		// Lógica de negócio específica para itens de Power User
		switch (originalAvatarCode) {
		case 204802: // PU 7 dias
			avatar.setItem(puItem); // Item real
			avatar.setExpire(Timestamp.valueOf(LocalDateTime.now().plusDays(7)));
			break;
		case 204803: // PU 14 dias
			avatar.setItem(puItem); // Item real
			avatar.setExpire(Timestamp.valueOf(LocalDateTime.now().plusDays(14)));
			break;
		case 204804: // PU 30 dias
			avatar.setItem(puItem); // Item real
			avatar.setExpire(Timestamp.valueOf(LocalDateTime.now().plusDays(30)));
			break;
		default:
			avatar.setItem(originalAvatarCode);
			avatar.setExpire(null);
			break;
		}
		return avatar;
	}

	@Override
	public boolean disableAvatar(PlayerSession player, int itemId) {
		return chestDAO.disableAvatar(itemId, player.getUserNameId());
	}

	@Override
	public boolean giftItem(PlayerSession player, int itemId, String receiver, String receiverNick, String text)
			throws ShopException {

		try {
			// Buscando o PlayerAvatar pelo idx
			Optional<PlayerAvatar> hasAvatar = player.getPlayerAvatars().stream().filter(p -> p.getIdx() == itemId)
					.findFirst();

			// Se nao tem o avatar pode ser tentativa de hacking
			if (!hasAvatar.isPresent()) {
				return false;
			} else {
				receiptService
						.insertReceiptGift(new ReceiptGiftDTO(hasAvatar.get(), player, receiver, receiverNick, text));
				player.getPlayerAvatars().removeIf(p -> p.getIdx() == itemId);
			}

			return chestDAO.updateOwnerId(hasAvatar.get().getIdx(), receiver);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ShopException("Ocorreu um erro no banco de dados durante o gift.");
		}
	}

	@Override
	public int sellItem(PlayerSession player, int itemId) throws ShopException {

		try {
			Double sellDivisor = 1.666666666666667;

			ChestDTO chestDTO = chestDAO.getByIdx(itemId);
			MenuDTO avatarData = gameMenu.getByNo(chestDTO.getItem());

			Double refund = 0.0;
			int priceRefund = (avatarData.getPriceByGoldForI()) != null ? avatarData.getPriceByGoldForI() : 0;

			if (priceRefund != 0 && sellDivisor != 0) {
				refund = priceRefund / sellDivisor;
			}

			int refundInt = (int) Math.ceil(refund);

			receiptService.insertReceiptSell(new ReceiptSellDTO(player, chestDTO, 0, refundInt));

			if (refundInt > 0) {
				// Atualiza valor em memoria
				player.setGold(player.getGold() + refundInt);

				// Atualiza valor no banco.
				userDAO.updateAddGold(player.getUserNameId(), refundInt);
			}
			
			//desativar no banco o avatar vendido
			chestDAO.disableAvatar(itemId, player.getUserNameId());

			return refundInt;

		} catch (Exception e) {
			e.printStackTrace();
			throw new ShopException("Ocorreu um erro no banco de dados durante a venda.");
		}
	}

}
