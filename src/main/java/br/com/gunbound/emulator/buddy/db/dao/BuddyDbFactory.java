package br.com.gunbound.emulator.buddy.db.dao;

import br.com.gunbound.emulator.buddy.db.dao.imp.BuddyJDBC;
import br.com.gunbound.emulator.buddy.db.dao.imp.BuddyUserJDBC;
import br.com.gunbound.emulator.buddy.db.dao.imp.PacketJDBC;

public class BuddyDbFactory {
	public static BuddyListDao CreateBuddyDao() {
		return new BuddyJDBC();
	}
    public static PacketDao createPacketDao() {
        return new PacketJDBC();
    }
    
    public static BuddyUserDAO createBuddyUserDao() {
        return new BuddyUserJDBC();
    }
}
