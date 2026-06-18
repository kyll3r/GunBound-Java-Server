package br.com.gunbound.emulator.buddy.db.services.impl;

import java.util.List;

import br.com.gunbound.emulator.buddy.db.dao.BuddyDbFactory;
import br.com.gunbound.emulator.buddy.db.dao.PacketDao;
import br.com.gunbound.emulator.buddy.db.services.PacketService;
import br.com.gunbound.emulator.buddy.entities.dto.PacketDTO;

public class PacketServiceImpl implements PacketService {
	private final PacketDao dao;

	// Construtor usa factory, mas pode ser DI.
	public PacketServiceImpl() {
		this.dao = BuddyDbFactory.createPacketDao();
	}

	@Override
	public int save(PacketDTO packet) {
		// Aqui pode validar se os campos obrigatórios estão preenchidos, se quiser
		if (packet.getReceiver() == null || packet.getSender() == null || packet.getBody() == null) {
			throw new IllegalArgumentException("Campos obrigatórios não preenchidos!");
		}
		return dao.insert(packet);
	}

	@Override
	public PacketDTO getBySerialNo(int serialNo) {
		return dao.getBySerialNo(serialNo);
	}

	@Override
	public List<PacketDTO> getByReceiver(String receiver) {
		return dao.getByReceiver(receiver);
	}
	
	@Override
	public boolean deleteBySerialNo(int serialNo) {
		return dao.deleteBySerialNo(serialNo);
	}
}