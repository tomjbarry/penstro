package com.py.py.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.CorrespondenceDao;
import com.py.py.dao.MessageDao;
import com.py.py.domain.Correspondence;
import com.py.py.domain.Message;
import com.py.py.domain.User;
import com.py.py.domain.subdomain.BinaryUserId;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.dto.in.SubmitMessageDTO;
import com.py.py.dto.out.ConversationDTO;
import com.py.py.dto.out.MessageDTO;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.NotFoundException;

public class MessageServiceImplTest extends BaseServiceTest {
	
	@Autowired
	@Qualifier("messageService")
	private MessageService messageService;
	
	@Autowired
	private MessageDao messageDao;
	
	@Autowired
	private CorrespondenceDao correspondenceDao;
	
	@Autowired
	private UserService userService;
	
	private User sourceUser = createValidUser();
	private User targetUser = createValidUser();
	private CachedUsername source = new CachedUsername(validUserId, validName);
	private CachedUsername target = new CachedUsername(new ObjectId(), validOtherName);
	private List<Message> validMessages = null;
	private List<Correspondence> validCorrespondences = null;
	
	@Before
	public void setUp() {
		reset(messageDao, userService);
		Message message1 = new Message();
		message1.setAuthor(source);
		message1.setTarget(target);
		message1.setMessage(validContent);
		Message message2 = new Message();
		message2.setAuthor(source);
		message2.setTarget(target);
		message2.setMessage(validContent);
		Message message3 = new Message();
		message3.setAuthor(source);
		message3.setTarget(target);
		message3.setMessage(validContent);
		
		BinaryUserId buid1 = new BinaryUserId(source.getId(), target.getId());
		BinaryUserId buid2 = new BinaryUserId(source.getId(), target.getId());
		BinaryUserId buid3 = new BinaryUserId(source.getId(), target.getId());
		
		CachedUsername a = source;
		CachedUsername b = target;
		if(buid1.getFirst() != source.getId()) {
			a = target;
			b = source;
		}
		
		Correspondence c1 = new Correspondence();
		c1.setId(buid1);
		c1.setFirst(a);
		c1.setSecond(b);
		c1.setAuthor(source.getId());
		c1.setTarget(target.getId());
		c1.setMessage(validContent);
		
		Correspondence c2 = new Correspondence();
		c2.setId(buid2);
		c2.setFirst(a);
		c2.setSecond(b);
		c2.setAuthor(source.getId());
		c2.setTarget(target.getId());
		c2.setMessage(validContent);
		
		Correspondence c3 = new Correspondence();
		c3.setId(buid3);
		c3.setFirst(a);
		c3.setSecond(b);
		c3.setAuthor(source.getId());
		c3.setTarget(target.getId());
		c3.setMessage(validContent);
		
		validMessages = Arrays.asList(message1, message2, message3);
		validCorrespondences = Arrays.asList(c1,c2,c3);
		
		sourceUser.setUsername(validName);
		targetUser.setUsername(validOtherName);
		sourceUser.setId(validUserId);
		targetUser.setId(new ObjectId());
	}
	
	@Test(expected = BadParameterException.class)
	public void getConversationDTOsNull1() throws Exception {
		messageService.getConversationDTOs(null, constructPageable(), randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getConversationDTOsNull2() throws Exception {
		messageService.getConversationDTOs(new User(), null, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getConversationDTOsInvalid() throws Exception {
		messageService.getConversationDTOs(createInvalidUser(), constructPageable(), randomBoolean());
	}
	
	@Test
	public void getConversationDTOsInvalidList() throws Exception {
		List<Correspondence> invalidCorrespondences = addNullToList(validCorrespondences);
		when(correspondenceDao.getCorrespondences(any(ObjectId.class), any(Pageable.class)))
				.thenReturn(new PageImpl<Correspondence>(invalidCorrespondences));
		Page<ConversationDTO> result = messageService.getConversationDTOs(
				sourceUser, constructPageable(), randomBoolean());
		Assert.assertEquals(validCorrespondences.size(), result.getContent().size());
	}
	
	@Test
	public void getConversationDTOs() throws Exception {
		when(correspondenceDao.getCorrespondences(any(ObjectId.class), any(Pageable.class)))
				.thenReturn(new PageImpl<Correspondence>(validCorrespondences));
		Page<ConversationDTO> result = messageService.getConversationDTOs(
				sourceUser, constructPageable(), false);
		Assert.assertEquals(validCorrespondences.size(), result.getContent().size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getMessageDTOsNull1() throws Exception {
		messageService.getMessageDTOs(null, targetUser, constructPageable(), randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getMessageDTOsNull2() throws Exception {
		messageService.getMessageDTOs(sourceUser, null, constructPageable(), randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getMessageDTOsNull3() throws Exception {
		messageService.getMessageDTOs(sourceUser, targetUser, null, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getMessageDTOsInvalid1() throws Exception {
		messageService.getMessageDTOs(createInvalidUser(), targetUser, constructPageable(), randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getMessageDTOsInvalid2() throws Exception {
		messageService.getMessageDTOs(sourceUser, createInvalidUser(), constructPageable(), randomBoolean());
	}
	
	@Test
	public void getMessageDTOsInvalidList() throws Exception {
		List<Message> invalidMessages = addNullToList(validMessages);
		when(userService.findUserByUsername(anyString())).thenReturn(targetUser);
		when(messageDao.getMessagesAll(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class)))
				.thenReturn(new PageImpl<Message>(invalidMessages));
		Page<MessageDTO> result = messageService.getMessageDTOs(
				sourceUser, targetUser, constructPageable(), randomBoolean());
		Assert.assertEquals(validMessages.size(), result.getContent().size());
	}
	
	@Test
	public void getMessageDTOs() throws Exception {
		when(messageDao.getMessagesAll(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class)))
				.thenReturn(new PageImpl<Message>(validMessages));
		when(userService.findUserByUsername(anyString()))
			.thenThrow(new NotFoundException(validName)).thenReturn(targetUser);
		
		Page<MessageDTO> result = messageService.getMessageDTOs(
				sourceUser, targetUser, constructPageable(), false);
		Assert.assertEquals(validMessages.size(), result.getContent().size());
		
		result = messageService.getMessageDTOs(
				sourceUser, targetUser, constructPageable(), false);
		Assert.assertEquals(validMessages.size(), result.getContent().size());
	}
	
	@Test(expected = BadParameterException.class)
	public void createMessageNull1() throws Exception {
		messageService.createMessage(null, targetUser, new SubmitMessageDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void createMessageNull2() throws Exception {
		messageService.createMessage(sourceUser, null, new SubmitMessageDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void createMessageNull3() throws Exception {
		messageService.createMessage(sourceUser, targetUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void createMessageInvalid1() throws Exception {
		messageService.createMessage(createInvalidUser(), targetUser, new SubmitMessageDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void createMessageInvalid2() throws Exception {
		messageService.createMessage(sourceUser, createInvalidUser(), new SubmitMessageDTO());
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void createMessageNotAllowed() throws Exception {
		User source = createValidUser();
		source.setUsername(validName);
		source.setId(validUserId);
		User target = createValidUser();
		target.setUsername(validName);
		target.setId(validUserId);
		
		Message message = new Message();
		message.setId(validUserId);
		SubmitMessageDTO dto = new SubmitMessageDTO();
		dto.setMessage(validContent);
		when(messageDao.save(any(Message.class))).thenReturn(message);
		messageService.createMessage(source, target, dto);
	}
	
	@Test
	public void createMessage() throws Exception {
		Message message = new Message();
		message.setId(validUserId);
		when(messageDao.save(any(Message.class))).thenReturn(message);
		
		SubmitMessageDTO dto = new SubmitMessageDTO();
		dto.setMessage(validContent);
		messageService.createMessage(sourceUser, targetUser, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void getMessageCountNull() throws Exception {
		messageService.getMessageCount(null, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getMessageCountInvalid() throws Exception {
		messageService.getMessageCount(createInvalidUser(), randomBoolean());
	}
	
	@Test
	public void getMessageCount() throws Exception {
		when(messageDao.getMessages(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class), any(Boolean.class)))
				.thenReturn(new PageImpl<Message>(validMessages));
		Assert.assertEquals(messageService.getMessageCount(sourceUser, true), 
				validMessages.size());
		Assert.assertEquals(messageService.getMessageCount(sourceUser, false), 
				validMessages.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void flagConversationNull1() throws Exception {
		messageService.flagConversation(null, createValidUserInfo(), targetUser, 
				createValidUserInfo());
	}
	
	@Test(expected = BadParameterException.class)
	public void flagConversationNull2() throws Exception {
		messageService.flagConversation(sourceUser, null, targetUser, createValidUserInfo());
	}
	
	@Test(expected = BadParameterException.class)
	public void flagConversationNull3() throws Exception {
		messageService.flagConversation(sourceUser, createValidUserInfo(), 
				null, createValidUserInfo());
	}
	
	@Test(expected = BadParameterException.class)
	public void flagConversationNull4() throws Exception {
		messageService.flagConversation(sourceUser, createValidUserInfo(), 
				targetUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagConversationInvalid1() throws Exception {
		messageService.flagConversation(createInvalidUser(), 
				createValidUserInfo(), targetUser, createValidUserInfo());
	}
	
	@Test(expected = BadParameterException.class)
	public void flagConversationInvalid3() throws Exception {
		messageService.flagConversation(sourceUser, createValidUserInfo(), 
				createInvalidUser(), createValidUserInfo());
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void flagConversationNotAllowed() throws Exception {
		User target = createValidUser();
		target.setUsername(validName);
		target.setId(validUserId);
		
		User source = createValidUser();
		source.setUsername(validName);
		source.setId(validUserId);
		
		messageService.flagConversation(source, createValidUserInfo(), 
				target, createValidUserInfo());
	}
	
	@Test
	public void flagConversation() throws Exception {
		messageService.flagConversation(sourceUser, createValidUserInfo(), 
				targetUser, createValidUserInfo());
	}
	
	@Test(expected = BadParameterException.class)
	public void getConversationDTONull1() throws Exception {
		messageService.getConversationDTO(null, targetUser);
	}
	
	@Test(expected = BadParameterException.class)
	public void getConversationDTONull2() throws Exception {
		messageService.getConversationDTO(sourceUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getConversationDTOInvalid1() throws Exception {
		messageService.getConversationDTO(createInvalidUser(), targetUser);
	}
	
	@Test(expected = BadParameterException.class)
	public void getConversationDTOInvalid2() throws Exception {
		messageService.getConversationDTO(sourceUser, createInvalidUser());
	}
	
	@Test(expected = NotFoundException.class)
	public void getConversationDTONotFound() throws Exception {
		when(correspondenceDao.findOne(any(BinaryUserId.class)))
				.thenReturn(null);
		messageService.getConversationDTO(sourceUser, targetUser);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void getConversationDTONotAllowed() throws Exception {
		Correspondence correspondence = new Correspondence();
		BinaryUserId buId = new BinaryUserId(sourceUser.getId(), targetUser.getId());
		correspondence.setId(buId);
		correspondence.setFirstHidden(true);
		correspondence.setSecondHidden(true);
		if(sourceUser.getId().equals(buId.getFirst())) {
			correspondence.setFirst(new CachedUsername(sourceUser.getId(), sourceUser.getUsername()));
			correspondence.setSecond(new CachedUsername(targetUser.getId(), targetUser.getUsername()));
		} else {
			correspondence.setFirst(new CachedUsername(targetUser.getId(), targetUser.getUsername()));
			correspondence.setSecond(new CachedUsername(sourceUser.getId(), sourceUser.getUsername()));
		}
		when(correspondenceDao.findOne(any(BinaryUserId.class)))
				.thenReturn(correspondence);
		messageService.getConversationDTO(sourceUser, targetUser);
	}
	
	@Test
	public void getConversationDTO() throws Exception {
		Correspondence correspondence = new Correspondence();
		BinaryUserId buId = new BinaryUserId(sourceUser.getId(), targetUser.getId());
		correspondence.setId(buId);
		correspondence.setFirstHidden(false);
		correspondence.setSecondHidden(false);
		if(sourceUser.getId().equals(buId.getFirst())) {
			correspondence.setFirst(new CachedUsername(sourceUser.getId(), sourceUser.getUsername()));
			correspondence.setSecond(new CachedUsername(targetUser.getId(), targetUser.getUsername()));
		} else {
			correspondence.setFirst(new CachedUsername(targetUser.getId(), targetUser.getUsername()));
			correspondence.setSecond(new CachedUsername(sourceUser.getId(), sourceUser.getUsername()));
		}
		when(correspondenceDao.findOne(any(BinaryUserId.class)))
				.thenReturn(correspondence);
		messageService.getConversationDTO(sourceUser, targetUser);
	}
	
	@Test(expected = BadParameterException.class)
	public void toggleShowConversationNull1() throws Exception {
		messageService.toggleShowConversation(null, targetUser, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void toggleShowConversationNull2() throws Exception {
		messageService.toggleShowConversation(sourceUser, null, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void toggleShowConversationInvalid1() throws Exception {
		messageService.toggleShowConversation(createInvalidUser(), targetUser, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void toggleShowConversationInvalid2() throws Exception {
		messageService.toggleShowConversation(sourceUser, createInvalidUser(), randomBoolean());
	}
	
	@Test
	public void toggleShowConversation() throws Exception {
		messageService.toggleShowConversation(sourceUser, targetUser, false);
		messageService.toggleShowConversation(sourceUser, targetUser, true);
		
	}
}
