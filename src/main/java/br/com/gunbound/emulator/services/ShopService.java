package br.com.gunbound.emulator.services;

import java.util.List;

import br.com.gunbound.emulator.model.entities.DTO.ChestDTO;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;
import br.com.gunbound.emulator.services.excepion.ShopException;

/**
 * Define o contrato para serviços que gerenciam o inventário (baú) dos
 * jogadores.
 */
public interface ShopService {

	/**
	 * Busca todos os itens no inventário de um jogador.
	 * 
	 * @param player A sessão do jogador.
	 * @return Uma lista de ChestDTO contendo os itens do jogador.
	 */
	List<ChestDTO> getPlayerItems(PlayerSession player);

	/**
	 * Executa a lógica de compra de um item para um jogador. Verifica o saldo,
	 * debita o valor e adiciona o item ao inventário.
	 * 
	 * @param player       A sessão do jogador que está comprando.
	 * @param avatarCode   O item a ser comprado
	 * @param currencyType Se é Gold/Cash
	 * @throws ShopException se a compra falhar (ex: saldo insuficiente).
	 */
	public ChestDTO buyAvatar(PlayerSession player, int avatarCode, int currencyType) throws ShopException;

	/**
	 * Executa a lógica de gift de um item do inventário de um jogador.
	 * 
	 * @param player A sessão do jogador que está vendendo.
	 * @param itemId O ID do item a ser vendido.
	 * @throws ShopException se a venda falhar.
	 */
	public boolean giftItem(PlayerSession player, int itemId, String receiver, String receiverNick, String text)
			throws ShopException;

	/**
	 * Executa a lógica de venda de um item do inventário de um jogador.
	 * 
	 * @param player A sessão do jogador que está vendendo.
	 * @param itemId O ID do item a ser vendido.
	 * @return int o valor a ser creditado da venda na UI
	 * @throws ShopException se a venda falhar.
	 */
	public int sellItem(PlayerSession player, int itemId) throws ShopException;

	//disable item
	public boolean disableAvatar(PlayerSession player, int itemId);
}
