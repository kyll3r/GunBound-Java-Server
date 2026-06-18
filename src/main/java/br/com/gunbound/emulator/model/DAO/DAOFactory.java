package br.com.gunbound.emulator.model.DAO;

import br.com.gunbound.emulator.model.DAO.impl.ChestJDBC;
import br.com.gunbound.emulator.model.DAO.impl.GameBuddyJDBC;
import br.com.gunbound.emulator.model.DAO.impl.MenuJDBC;
import br.com.gunbound.emulator.model.DAO.impl.PlayLogJDBC;
import br.com.gunbound.emulator.model.DAO.impl.StatsJDBC;
import br.com.gunbound.emulator.model.DAO.impl.UserJDBC;
import br.com.gunbound.emulator.model.DAO.impl.receipts.ReceiptBuyJDBC;
import br.com.gunbound.emulator.model.DAO.impl.receipts.ReceiptGiftJDBC;
import br.com.gunbound.emulator.model.DAO.impl.receipts.ReceiptLogJDBC;
import br.com.gunbound.emulator.model.DAO.impl.receipts.ReceiptSellJDBC;
import br.com.gunbound.emulator.model.DAO.receipts.ReceiptBuyDAO;
import br.com.gunbound.emulator.model.DAO.receipts.ReceiptGiftDAO;
import br.com.gunbound.emulator.model.DAO.receipts.ReceiptLogDAO;
import br.com.gunbound.emulator.model.DAO.receipts.ReceiptSellDAO;

public class DAOFactory {

	public static UserDAO CreateUserDao() {
		return new UserJDBC();
	}

	public static ChestDAO CreateChestDao() {
		return new ChestJDBC();
	}

	public static MenuDAO CreateMenuDao() {
		return new MenuJDBC();
	}

	public static PlayLogDAO CreatePlayLogDao() {
		return new PlayLogJDBC();
	}

	public static StatsDAO CreateStatsDao() {
		return new StatsJDBC();
	}

	public static ReceiptGiftDAO CreateReceiptGiftDao() {
		return new ReceiptGiftJDBC();
	}

	public static ReceiptBuyDAO CreateReceiptBuyDao() {
		return new ReceiptBuyJDBC();
	}

	public static ReceiptSellDAO CreateReceiptSellDao() {
		return new ReceiptSellJDBC();
	}
	
	public static ReceiptLogDAO CreateReceiptLogDao() {
		return new ReceiptLogJDBC();
	}
	
	public static GameBuddyDAO CreateGameBuddyDAO() {
		return new GameBuddyJDBC();
	}

}
