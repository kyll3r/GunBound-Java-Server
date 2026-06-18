package br.com.gunbound.emulator.buddy.db.dao;

import java.util.List;

import br.com.gunbound.emulator.buddy.entities.dto.PacketDTO;

public interface PacketDao {
	int insert(PacketDTO packet);

	PacketDTO getBySerialNo(int serialNo);

	List<PacketDTO> getByReceiver(String receiver);
	
	boolean deleteBySerialNo(int serialNo);

}
