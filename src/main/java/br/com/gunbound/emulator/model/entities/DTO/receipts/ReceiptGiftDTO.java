package br.com.gunbound.emulator.model.entities.DTO.receipts;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import br.com.gunbound.emulator.model.entities.game.PlayerAvatar;
import br.com.gunbound.emulator.model.entities.game.PlayerSession;

public class ReceiptGiftDTO {
	 private int idx;             // Idx
	    private int idxChest;        // Idx_chest
	    private Integer menuId;      // MenuId (pode ser null)
	    private Integer volume;         // Volume (pode ser null)
	    private String sender;       // Sender
	    private String senderNick;       // SenderNick
	    private String receiver;     // Receiver
	    private String receiverNick; // ReceiverNick
	    private Timestamp giftTime;       // GiftTime
	    private String expireType;   // Expiretype
	    private String text;
	    private Timestamp confirmTime;    // ConfirmTime
	    private Integer confirmed;    // Confirmed (padrão '0')

	    public ReceiptGiftDTO() {
	    }
	    
	    public ReceiptGiftDTO(PlayerAvatar playerAvatar, PlayerSession ps, String receiver, String receiverNick, String text) {
	    	this.idxChest = playerAvatar.getIdx();
	    	this.menuId = playerAvatar.getItem();
	    	this.volume = playerAvatar.getVolume();
	    	this.sender = ps.getUserNameId();
	    	this.setSenderNick(ps.getNickName());
		    this.receiver = receiver;
		    this.receiverNick = receiverNick;
		    this.giftTime = Timestamp.valueOf(LocalDateTime.now());
	        this.expireType = playerAvatar.getExpireType();
	        this.text = text;
	    }
	    
	    // Construtor
	    public ReceiptGiftDTO(int idx, int idxChest, Integer menuId, Integer volume, String sender, 
	    		String senderNick, String receiver, String receiverNick, Timestamp giftTime, String expireType, 
	                       String text, Timestamp confirmTime, Integer confirmed) {
	        this.idx = idx;
	        this.idxChest = idxChest;
	        this.menuId = menuId;
	        this.volume = volume;
	        this.sender = sender;
	        this.setSenderNick(senderNick);
	        this.receiver = receiver;
	        this.receiverNick = receiverNick;
	        this.giftTime = giftTime;
	        this.expireType = expireType;
	        this.text = text;
	        this.confirmTime = confirmTime;
	        this.confirmed = confirmed != null ? confirmed : 0; // Padrão para '0' se for nulo
	    }

	    // Getters e Setters
	    public int getIdx() {
	        return idx;
	    }

	    public void setIdx(int idx) {
	        this.idx = idx;
	    }

	    public int getIdxChest() {
	        return idxChest;
	    }

	    public void setIdxChest(int idxChest) {
	        this.idxChest = idxChest;
	    }

	    public Integer getMenuId() {
	        return menuId;
	    }

	    public void setMenuId(Integer menuId) {
	        this.menuId = menuId;
	    }

	    public Integer getVolume() {
	        return volume;
	    }

	    public void setVolume(Integer volume) {
	        this.volume = volume;
	    }

	    public String getSender() {
	        return sender;
	    }

	    public void setSender(String sender) {
	        this.sender = sender;
	    }
	    
		public String getSenderNick() {
			return senderNick;
		}

		public void setSenderNick(String senderNick) {
			this.senderNick = senderNick;
		}

	    public String getReceiver() {
	        return receiver;
	    }

	    public void setReceiver(String receiver) {
	        this.receiver = receiver;
	    }

	    public String getReceiverNick() {
	        return receiverNick;
	    }

	    public void setReceiverNick(String receiverNick) {
	        this.receiverNick = receiverNick;
	    }

	    public Timestamp getGiftTime() {
	        return giftTime;
	    }

	    public void setGiftTime(Timestamp giftTime) {
	        this.giftTime = giftTime;
	    }

	    public String getExpireType() {
	        return expireType;
	    }

	    public void setExpireType(String expireType) {
	        this.expireType = expireType;
	    }
	    
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}

	    public Timestamp getConfirmTime() {
	        return confirmTime;
	    }

	    public void setConfirmTime(Timestamp confirmTime) {
	        this.confirmTime = confirmTime;
	    }

	    public Integer getConfirmed() {
	        return confirmed != null ? confirmed : 0;
	    }

	    public void setConfirmed(Integer confirmed) {
	        this.confirmed = confirmed;
	    }

	    @Override
	    public String toString() {
	        return "ReceiptGift{" +
	                "idx=" + idx +
	                ", idxChest=" + idxChest +
	                ", menuId=" + menuId +
	                ", volume=" + volume +
	                ", sender='" + sender + '\'' +
	                ", receiver='" + receiver + '\'' +
	                ", receiverNick='" + receiverNick + '\'' +
	                ", giftTime=" + giftTime +
	                ", expireType='" + expireType + '\'' +
	                ", confirmTime=" + confirmTime +
	                ", confirmed='" + confirmed + '\'' +
	                '}';
	    }
}
