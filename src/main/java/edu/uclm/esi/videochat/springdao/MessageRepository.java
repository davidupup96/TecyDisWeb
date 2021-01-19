package edu.uclm.esi.videochat.springdao;

import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import edu.uclm.esi.videochat.model.Message;

public interface MessageRepository extends CrudRepository <Message, String> {

	public Enumeration<Message> findByRecipientAndSender( String recipient, String sender);
	
	@Query(value = "SELECT * FROM message WHERE recipient=:recipient and sender=:sender OR recipient=:sender and sender=:recipient ", nativeQuery = true)
	public Vector<Message> findByRecipiente( String recipient, String sender);
	
	@Query(value = "SELECT count(*) FROM message WHERE recipient=:recipient and sender=:sender", nativeQuery = true)
	public int checkRecupera(@Param("recipient") String recipient,@Param("sender") String sender);

}
