package br.com.gunbound.emulator.buddy.db.services;

import java.util.List;

import br.com.gunbound.emulator.buddy.entities.dto.PacketDTO;

public interface PacketService {
	int save(PacketDTO packet);

	PacketDTO getBySerialNo(int serialNo);

	List<PacketDTO> getByReceiver(String receiver);
	
	boolean deleteBySerialNo(int serialNo);
}
