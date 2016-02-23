package com.py.py.service;
/*
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.EscrowDao;
import com.py.py.domain.Escrow;
import com.py.py.domain.User;
import com.py.py.domain.enumeration.ESCROW_TYPE;
import com.py.py.domain.subdomain.Balance;
import com.py.py.dto.in.BackingEmailOfferDTO;
import com.py.py.dto.in.BackingOfferDTO;
import com.py.py.dto.out.BackerDTO;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BackerNotFoundException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.NotFoundException;
*/
public class EscrowServiceImplTest extends BaseServiceTest {
/*
	@Autowired
	@Qualifier("escrowService")
	private EscrowService escrowService;
	
	@Autowired
	protected EscrowDao escrowDao;
	
	@Autowired
	protected UserService userService;
	
	@Autowired
	protected FinanceService financeService;
	
	@Autowired
	protected EventService eventService;
	
	@Autowired
	protected EmailService emailService;

	private BackingOfferDTO validBackingOffer = new BackingOfferDTO();
	private BackingOfferDTO invalidBackingOffer = new BackingOfferDTO();

	private BackingEmailOfferDTO validBackingEmailOffer = new BackingEmailOfferDTO();
	private BackingEmailOfferDTO invalidBackingEmailOffer = new BackingEmailOfferDTO();
	private List<Escrow> validEscrows = new ArrayList<Escrow>();
	private List<Escrow> invalidEscrows = new ArrayList<Escrow>();
	private User otherUser = new User();
	private Escrow validOffer = new Escrow();
	private Escrow validEmailOffer = new Escrow();
	private Escrow validBacking = new Escrow();
	private String validOtherEmail = "escrowemail@email.org";
	
	@Before
	public void setUp() {
		reset(escrowDao, userService, financeService, eventService, emailService);
		validBackingOffer.setUsername(validOtherName);
		invalidBackingOffer.setUsername(null);
		validBackingEmailOffer.setEmail(validEmail);
		invalidBackingEmailOffer.setEmail(null);
		
		otherUser = createValidUser();
		otherUser.setUsername("ESCROW_test");
		otherUser.setEmail("ESCROW_test@test.com");
		otherUser.setId(new ObjectId());
		
		Balance balance = new Balance();
		Escrow escrow1 = new Escrow();
		escrow1.setSource((new ObjectId()).toHexString());
		escrow1.setTarget((new ObjectId()).toHexString());
		escrow1.setType(ESCROW_TYPE.BACKING);
		balance.setGold(5L);
		escrow1.setBalance(balance);
		validBacking = escrow1;
		Escrow escrow2 = new Escrow();
		escrow2.setSource((new ObjectId()).toHexString());
		escrow2.setTarget(validEmail);
		escrow2.setType(ESCROW_TYPE.EMAIL_OFFER);
		balance = new Balance();
		balance.setGold(500L);
		escrow2.setBalance(balance);
		validEmailOffer = escrow2;
		Escrow escrow3 = new Escrow();
		escrow3.setSource(validUserId.toHexString());
		escrow3.setTarget((new ObjectId()).toHexString());
		escrow3.setType(ESCROW_TYPE.OFFER);
		balance = new Balance();
		balance.setGold(15L);
		escrow3.setBalance(balance);
		validOffer = escrow3;
		validEscrows.add(escrow1);
		validEscrows.add(escrow2);
		validEscrows.add(escrow3);
		

		Escrow escrow4 = new Escrow();
		escrow4.setSource(validUserId.toHexString());
		escrow4.setTarget((new ObjectId()).toHexString());
		escrow4.setType(ESCROW_TYPE.OFFER);
		balance = new Balance();
		balance.setGold(0L);
		escrow4.setBalance(balance);
		invalidEscrows.add(escrow1);
		invalidEscrows.add(escrow2);
		invalidEscrows.add(escrow3);
		invalidEscrows.add(null);
		invalidEscrows.add(escrow4);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerEscrowIdNull1() throws Exception {
		escrowService.getBackerEscrowId(null, validUserId);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerEscrowIdNull2() throws Exception {
		escrowService.getBackerEscrowId(validUserId, null);
	}
	
	@Test(expected = NotFoundException.class)
	public void getBackerEscrowIdNotFound() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null);
		escrowService.getBackerEscrowId(validUserId, validObjectId);
	}
	
	@Test
	public void getBackerEscrowId() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(new Escrow());
		escrowService.getBackerEscrowId(validUserId, validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerEscrowNull2() throws Exception {
		escrowService.getBackerEscrow(validUserId, null, validObjectId, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerEscrowNull4() throws Exception {
		escrowService.getBackerEscrow(validUserId, validName, validObjectId, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerEscrowInvalid2() throws Exception {
		escrowService.getBackerEscrow(validUserId, invalidName, validObjectId, 
				validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferBackerInvalid4() throws Exception {
		escrowService.getBackerEscrow(validUserId, validName, validObjectId, invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void getBackerEscrowNotFound() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null);
		escrowService.getBackerEscrow(validUserId, validName, validObjectId, validOtherName);
	}
	
	@Test
	public void getBackerEscrow() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(new Escrow());
		escrowService.getBackerEscrow(validUserId, validName, validObjectId, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferEscrowNull2() throws Exception {
		escrowService.getOfferEscrow(validUserId, null, validObjectId, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferEscrowNull4() throws Exception {
		escrowService.getOfferEscrow(validUserId, validName, validObjectId, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferEscrowInvalid2() throws Exception {
		when(userService.findUserByUsername(anyString())).thenReturn(otherUser);
		escrowService.getOfferEscrow(validUserId, invalidName, validObjectId, 
				validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferEscrowInvalid4() throws Exception {
		when(userService.findUserByUsername(anyString())).thenReturn(otherUser);
		escrowService.getOfferEscrow(validUserId, validName, validObjectId, invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void getOfferEscrowNotFound() throws Exception {
		when(userService.findUserByUsername(anyString())).thenReturn(otherUser);
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null);
		escrowService.getOfferEscrow(validUserId, validName, validObjectId, validOtherName);
	}
	
	@Test
	public void getOfferEscrow() throws Exception {
		when(userService.findUserByUsername(anyString())).thenReturn(otherUser);
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(new Escrow());
		escrowService.getOfferEscrow(validUserId, validName, validObjectId, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getEmailOfferEscrowNull2() throws Exception {
		when(userService.findUserByUsername(anyString())).thenReturn(otherUser);
		when(userService.findUserByEmail(anyString())).thenReturn(otherUser);
		escrowService.getEmailOfferEscrow(validUserId, null, validEmail);
	}
	
	@Test(expected = BadParameterException.class)
	public void getEmailOfferEscrowNull3() throws Exception {
		when(userService.findUserByUsername(anyString())).thenReturn(otherUser);
		when(userService.findUserByEmail(anyString())).thenReturn(otherUser);
		escrowService.getEmailOfferEscrow(validUserId, validName, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getEmailOfferEscrowInvalid2() throws Exception {
		when(userService.findUserByUsername(anyString())).thenReturn(otherUser);
		when(userService.findUserByEmail(anyString())).thenReturn(otherUser);
		escrowService.getEmailOfferEscrow(validUserId, invalidName, validEmail);
	}
	
	@Test(expected = BadParameterException.class)
	public void getEmailOfferEscrowInvalid3() throws Exception {
		when(userService.findUserByUsername(anyString())).thenReturn(otherUser);
		when(userService.findUserByEmail(anyString())).thenReturn(otherUser);
		escrowService.getEmailOfferEscrow(validUserId, validName, invalidEmail);
	}
	
	@Test(expected = NotFoundException.class)
	public void getEmailOfferEscrowNotFound() throws Exception {
		when(userService.findUserByUsername(anyString())).thenReturn(otherUser);
		when(userService.findUserByEmail(anyString())).thenReturn(otherUser);
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null);
		escrowService.getEmailOfferEscrow(validUserId, validName, validEmail);
	}
	
	@Test
	public void getEmailOfferEscrow() throws Exception {
		when(userService.findUserByUsername(anyString())).thenReturn(otherUser);
		when(userService.findUserByEmail(anyString())).thenReturn(otherUser);
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(new Escrow());
		escrowService.getEmailOfferEscrow(validUserId, validName, validEmail);
	}
	
	@Test(expected = BadParameterException.class)
	public void addOfferNull1() throws Exception {
		escrowService.addOffer(null, createValidUser(), validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void addOfferNull2() throws Exception {
		escrowService.addOffer(createValidUser(), null, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void addOfferInvalid1() throws Exception {
		escrowService.addOffer(createInvalidUser(), createValidUser(), validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void addOfferInvalid2() throws Exception {
		escrowService.addOffer(createValidUser(), createInvalidUser(), validCost);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void addOfferNotAllowed() throws Exception {
		User user1 = createValidUser();
		User user2 = createValidUser();
		user2.setId(user1.getId());
		user2.setUsername(user1.getUsername());
		escrowService.addOffer(user1, user2, validCost);
	}
	
	@Test
	public void addOffer() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(new Escrow());
		escrowService.addOffer(createValidUser(), otherUser, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void addEmailOfferNull1() throws Exception {
		escrowService.addEmailOffer(null, createValidUser(), validOtherEmail, 
				validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void addEmailOfferNull3() throws Exception {
		escrowService.addEmailOffer(createValidUser(), otherUser, null, 
				validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void addEmailOfferInvalid1() throws Exception {
		escrowService.addEmailOffer(createInvalidUser(), createValidUser(), 
				validOtherEmail, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void addEmailOfferInvalid2() throws Exception {
		escrowService.addEmailOffer(createValidUser(), createInvalidUser(), validOtherEmail, 
				validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void addEmailOfferInvalid3() throws Exception {
		escrowService.addEmailOffer(createValidUser(), otherUser, invalidEmail, 
				validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void addEmailOfferInvalid4() throws Exception {
		escrowService.addEmailOffer(createValidUser(), createValidUser(), validOtherEmail, 
				0L);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void addEmailOfferNotAllowed1() throws Exception {
		User user = createValidUser();
		user.setEmail(validEmail);
		User user2 = createValidUser();
		user2.setUsername(user.getUsername());
		user2.setId(user.getId());
		user2.setEmail(validOtherEmail);
		escrowService.addEmailOffer(user, user2, validOtherEmail, validCost);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void addEmailOfferNotAllowed2() throws Exception {
		User user = createValidUser();
		user.setEmail(validEmail);
		User user2 = createValidUser();
		user2.setUsername(validOtherName);
		user2.setId(new ObjectId());
		user2.setEmail(validEmail);
		escrowService.addEmailOffer(user, user2, validEmail, validCost);
	}
	
	@Test
	public void addEmailOffer() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(new Escrow());
		User user = createValidUser();
		user.setEmail(validEmail);
		User user2 = createValidUser();
		user2.setUsername(validOtherName);
		user2.setId(new ObjectId());
		user2.setEmail(validOtherEmail);
		escrowService.addEmailOffer(user, user2, validOtherEmail, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackersOutstandingNull1() throws Exception {
		escrowService.getBackersOutstanding(null, constructPageable());
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackersOutstandingNull2() throws Exception {
		escrowService.getBackersOutstanding(validUserId, null);
	}
	
	@Test
	public void getBackersOutstandingInvalidList() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(invalidEscrows));
		Page<BackerDTO> result = escrowService.getBackersOutstanding(validUserId, 
				constructPageable());
		Assert.assertEquals(result.getContent().size(), validEscrows.size());
	}
	
	@Test
	public void getBackersOutstanding() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(validEscrows));
		Page<BackerDTO> result = escrowService.getBackersOutstanding(validUserId, 
				constructPageable());
		Assert.assertEquals(result.getContent().size(), validEscrows.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getOffersOutstandingNull1() throws Exception {
		escrowService.getOffersOutstanding(null, constructPageable());
	}
	
	@Test(expected = BadParameterException.class)
	public void getOffersOutstandingNull2() throws Exception {
		escrowService.getOffersOutstanding(validUserId, null);
	}
	
	@Test
	public void getOffersOutstandingInvalidList() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(invalidEscrows));
		Page<BackerDTO> result = escrowService.getOffersOutstanding(validUserId, 
				constructPageable());
		Assert.assertEquals(result.getContent().size(), validEscrows.size());
	}
	
	@Test
	public void getOffersOutstanding() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(validEscrows));
		Page<BackerDTO> result = escrowService.getOffersOutstanding(validUserId, 
				constructPageable());
		Assert.assertEquals(result.getContent().size(), validEscrows.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getEmailOffersOutstandingNull1() throws Exception {
		escrowService.getEmailOffersOutstanding(null, constructPageable());
	}
	
	@Test(expected = BadParameterException.class)
	public void getEmailOffersOutstandingNull2() throws Exception {
		escrowService.getEmailOffersOutstanding(validUserId, null);
	}
	
	@Test
	public void getEmailOffersOutstandingInvalidList() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(invalidEscrows));
		Page<BackerDTO> result = escrowService.getEmailOffersOutstanding(validUserId, 
				constructPageable());
		Assert.assertEquals(result.getContent().size(), validEscrows.size());
	}
	
	@Test
	public void getEmailOffersOutstanding() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(validEscrows));
		Page<BackerDTO> result = escrowService.getEmailOffersOutstanding(validUserId, 
				constructPageable());
		Assert.assertEquals(result.getContent().size(), validEscrows.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackersNull1() throws Exception {
		escrowService.getBackers(null, constructPageable());
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackersNull2() throws Exception {
		escrowService.getBackers(validUserId, null);
	}
	
	@Test
	public void getBackersInvalidList() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(invalidEscrows));
		Page<BackerDTO> result = escrowService.getBackers(validUserId, 
				constructPageable());
		Assert.assertEquals(result.getContent().size(), validEscrows.size());
	}
	
	@Test
	public void getBackers() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(validEscrows));
		Page<BackerDTO> result = escrowService.getBackers(validUserId, 
				constructPageable());
		Assert.assertEquals(result.getContent().size(), validEscrows.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getOffersNull1() throws Exception {
		escrowService.getOffers(null, constructPageable());
	}
	
	@Test(expected = BadParameterException.class)
	public void getOffersNull2() throws Exception {
		escrowService.getOffers(createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOffersInvalid() throws Exception {
		escrowService.getOffers(createInvalidUser(), constructPageable());
	}
	
	@Test
	public void getOffersInvalidList() throws Exception {
		when(escrowDao.findSortedMulti(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(invalidEscrows));
		Page<BackerDTO> result = escrowService.getOffers(createValidUser(), 
				constructPageable());
		Assert.assertEquals(result.getContent().size(), validEscrows.size());
	}
	
	@Test
	public void getOffers() throws Exception {
		when(escrowDao.findSortedMulti(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(validEscrows));
		Page<BackerDTO> result = escrowService.getOffers(createValidUser(), 
				constructPageable());
		Assert.assertEquals(result.getContent().size(), validEscrows.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void acceptOfferNull1() throws Exception {
		escrowService.acceptOffer(null, createValidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void acceptOfferNull3() throws Exception {
		escrowService.acceptOffer(createValidUser(), otherUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void acceptOfferInvalid1() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(new ArrayList<Escrow>(), new PageRequest(0,1), 0));
		escrowService.acceptOffer(createInvalidUser(), createValidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void acceptOfferInvalid2() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(new ArrayList<Escrow>(), new PageRequest(0,1), 0));
		escrowService.acceptOffer(createValidUser(), createInvalidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void acceptOfferInvalid3() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(new ArrayList<Escrow>(), new PageRequest(0,1), 0));
		escrowService.acceptOffer(createValidUser(), otherUser, invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void acceptOfferNotFound() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(new ArrayList<Escrow>(), new PageRequest(0,1), 0));
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null);
		User source = createValidUser();
		source.setUsername(validOtherName);
		source.setEmail(validOtherEmail);
		source.setId(new ObjectId());
		escrowService.acceptOffer(createValidUser(), source, validOtherName);
	}
	
	@Test(expected = BackerNotFoundException.class)
	public void acceptOfferBackerNotFound() throws Exception {
		Escrow escrow1 = new Escrow();
		escrow1.setSource(validUserId.toHexString());
		escrow1.setTarget((new ObjectId()).toHexString());
		escrow1.setType(ESCROW_TYPE.OFFER);
		Balance balance = new Balance();
		balance.setGold(5L);
		escrow1.setBalance(balance);
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(new ArrayList<Escrow>(), new PageRequest(0,1), 0));
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(escrow1);
		User source = createValidUser();
		source.setUsername(validOtherName);
		source.setEmail(validOtherEmail);
		source.setId(new ObjectId());
		escrowService.acceptOffer(createValidUser(), null, validOtherName);
	}
	
	@Test
	public void acceptOffer() throws Exception {
		Escrow escrow1 = new Escrow();
		escrow1.setSource(validUserId.toHexString());
		escrow1.setTarget((new ObjectId()).toHexString());
		escrow1.setType(ESCROW_TYPE.OFFER);
		Balance balance = new Balance();
		balance.setGold(5L);
		escrow1.setBalance(balance);
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(new ArrayList<Escrow>(), new PageRequest(0,1), 0));
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(escrow1);
		User source = createValidUser();
		source.setUsername(validOtherName);
		source.setEmail(validOtherEmail);
		source.setId(new ObjectId());
		escrowService.acceptOffer(createValidUser(), source, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void acceptEmailOfferNull1() throws Exception {
		escrowService.acceptEmailOffer(null, createValidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void acceptEmailOfferNull3() throws Exception {
		escrowService.acceptEmailOffer(createValidUser(), otherUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void acceptEmailOfferInvalid1() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(new ArrayList<Escrow>(), new PageRequest(0,1), 0));
		escrowService.acceptEmailOffer(createInvalidUser(), createValidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void acceptEmailOfferInvalid2() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(new ArrayList<Escrow>(), new PageRequest(0,1), 0));
		escrowService.acceptEmailOffer(createValidUser(), createInvalidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void acceptEmailOfferInvalid3() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(new ArrayList<Escrow>(), new PageRequest(0,1), 0));
		escrowService.acceptOffer(createValidUser(), otherUser, invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void acceptEmailOfferNotFound() throws Exception {
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(new ArrayList<Escrow>(), new PageRequest(0,1), 0));
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null);
		User source = createValidUser();
		source.setUsername(validOtherName);
		source.setEmail(validOtherEmail);
		source.setId(new ObjectId());
		escrowService.acceptEmailOffer(createValidUser(), source, validOtherName);
	}
	
	@Test(expected = BackerNotFoundException.class)
	public void acceptEmailOfferBackerNotFoundException() throws Exception {
		Balance balance = new Balance();
		Escrow escrow2 = new Escrow();
		escrow2.setSource(validUserId.toHexString());
		escrow2.setTarget(validEmail);
		escrow2.setType(ESCROW_TYPE.EMAIL_OFFER);
		balance = new Balance();
		balance.setGold(5L);
		escrow2.setBalance(balance);
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(new ArrayList<Escrow>(), new PageRequest(0,1), 0));
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(escrow2).thenReturn(escrow2);
		User source = createValidUser();
		source.setUsername(validOtherName);
		source.setEmail(validOtherEmail);
		source.setId(new ObjectId());
		escrowService.acceptEmailOffer(createValidUser(), null, validOtherName);
	}
	
	@Test
	public void acceptEmailOffer() throws Exception {
		Balance balance = new Balance();
		Escrow escrow2 = new Escrow();
		escrow2.setSource(validUserId.toHexString());
		escrow2.setTarget(validEmail);
		escrow2.setType(ESCROW_TYPE.EMAIL_OFFER);
		balance = new Balance();
		balance.setGold(5L);
		escrow2.setBalance(balance);
		when(escrowDao.findSorted(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString(), any(Pageable.class)))
				.thenReturn(new PageImpl<Escrow>(new ArrayList<Escrow>(), new PageRequest(0,1), 0));
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(escrow2).thenReturn(escrow2);
		User source = createValidUser();
		source.setUsername(validOtherName);
		source.setEmail(validOtherEmail);
		source.setId(new ObjectId());
		escrowService.acceptEmailOffer(createValidUser(), source, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void denyOfferNull1() throws Exception {
		escrowService.denyOffer(null, createValidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void denyOfferNull3() throws Exception {
		escrowService.denyOffer(createValidUser(), otherUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void denyOfferInvalid1() throws Exception {
		escrowService.denyOffer(createInvalidUser(), createValidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void denyOfferInvalid2() throws Exception {
		escrowService.denyOffer(createValidUser(), createInvalidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void denyOfferInvalid3() throws Exception {
		escrowService.denyOffer(createValidUser(), otherUser, invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void denyOfferNotFound() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null).thenReturn(null);
		escrowService.denyOffer(createValidUser(), otherUser, validOtherName);
	}
	
	@Test
	public void denyOffer() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(validOffer).thenReturn(validOffer);
		escrowService.denyOffer(createValidUser(), null, validOtherName);
		escrowService.denyOffer(createValidUser(), otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void denyEmailOfferNull1() throws Exception {
		escrowService.denyEmailOffer(null, createValidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void denyEmailOfferNull3() throws Exception {
		escrowService.denyEmailOffer(createValidUser(), otherUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void denyEmailOfferInvalid1() throws Exception {
		escrowService.denyEmailOffer(createInvalidUser(), createValidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void denyEmailOfferInvalid2() throws Exception {
		escrowService.denyEmailOffer(createValidUser(), createInvalidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void denyEmailOfferInvalid3() throws Exception {
		escrowService.denyEmailOffer(createValidUser(), otherUser, invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void denyEmailOfferNotFound() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null).thenReturn(null);
		escrowService.denyEmailOffer(createValidUser(), otherUser, validOtherName);
	}
	
	@Test
	public void denyEmailOffer() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(validEmailOffer).thenReturn(validEmailOffer);
		escrowService.denyEmailOffer(createValidUser(), null, validOtherName);
		escrowService.denyEmailOffer(createValidUser(), otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawOfferNull1() throws Exception {
		escrowService.withdrawOffer(null, createValidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawOfferNull2() throws Exception {
		escrowService.withdrawOffer(createValidUser(), otherUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawOfferInvalid1() throws Exception {
		escrowService.withdrawOffer(createInvalidUser(), createValidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawOfferInvalid2() throws Exception {
		escrowService.withdrawOffer(createValidUser(), createInvalidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawOfferInvalid3() throws Exception {
		escrowService.withdrawOffer(createValidUser(), otherUser, invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void withdrawOfferNotFound() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null);
		escrowService.withdrawOffer(createValidUser(), otherUser, validOtherName);
	}
	
	@Test
	public void withdrawOffer() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(validOffer);
		escrowService.withdrawOffer(createValidUser(), otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawEmailOfferNull1() throws Exception {
		escrowService.withdrawEmailOffer(null, createValidUser(), validEmail);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawEmailOfferNull3() throws Exception {
		escrowService.withdrawEmailOffer(createValidUser(), otherUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawEmailOfferInvalid1() throws Exception {
		escrowService.withdrawEmailOffer(createInvalidUser(), createValidUser(), validEmail);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawEmailOfferInvalid2() throws Exception {
		escrowService.withdrawEmailOffer(createValidUser(), createInvalidUser(), validEmail);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawEmailOfferInvalid3() throws Exception {
		escrowService.withdrawEmailOffer(createValidUser(), otherUser, invalidEmail);
	}
	
	@Test(expected = NotFoundException.class)
	public void withdrawEmailOfferNotFound() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null);
		escrowService.withdrawEmailOffer(createValidUser(), otherUser, validEmail);
	}
	
	@Test
	public void withdrawEmailOffer() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(validEmailOffer);
		escrowService.withdrawEmailOffer(createValidUser(), otherUser, validEmail);
	}
	
	@Test(expected = BadParameterException.class)
	public void cancelBackingNull1() throws Exception {
		escrowService.cancelBacking(null, createValidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void cancelBackingNull3() throws Exception {
		escrowService.cancelBacking(createValidUser(), otherUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void cancelBackingInvalid1() throws Exception {
		escrowService.cancelBacking(createInvalidUser(), createValidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void cancelBackingInvalid2() throws Exception {
		escrowService.cancelBacking(createValidUser(), createInvalidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void cancelBackingInvalid3() throws Exception {
		escrowService.cancelBacking(createValidUser(), otherUser, invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void cancelBackingNotFound() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null);
		escrowService.cancelBacking(createValidUser(), otherUser, validOtherName);
	}
	
	@Test
	public void cancelBacking() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(validBacking);
		escrowService.cancelBacking(createValidUser(), otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawBackingNull1() throws Exception {
		escrowService.withdrawBacking(null, createValidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawBackingNull3() throws Exception {
		escrowService.withdrawBacking(createValidUser(), otherUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawBackingInvalid1() throws Exception {
		escrowService.withdrawBacking(createInvalidUser(), createValidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawBackingInvalid2() throws Exception {
		escrowService.withdrawBacking(createValidUser(), createInvalidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void withdrawBackingInvalid3() throws Exception {
		escrowService.withdrawBacking(createValidUser(), otherUser, invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void withdrawBackingNotFound() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null);
		escrowService.withdrawBacking(createValidUser(), otherUser, validOtherName);
	}
	
	@Test
	public void withdrawBacking() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(validBacking);
		escrowService.withdrawBacking(createValidUser(), otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerOutstandingDTONull1() throws Exception {
		escrowService.getBackerOutstandingDTO(null, otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerOutstandingDTONull3() throws Exception {
		escrowService.getBackerOutstandingDTO(createValidUser(), otherUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerOutstandingDTOInvalid1() throws Exception {
		escrowService.getBackerOutstandingDTO(createInvalidUser(), otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerOutstandingDTOInvalid2() throws Exception {
		escrowService.getBackerOutstandingDTO(createValidUser(), 
				createInvalidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerOutstandingDTOInvalid3() throws Exception {
		escrowService.getBackerOutstandingDTO(createValidUser(), 
				otherUser, invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void getBackerOutstandingDTONotFound() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null);
		escrowService.getBackerOutstandingDTO(createValidUser(), otherUser, validOtherName);
	}
	
	@Test
	public void getBackerOutstandingDTO() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(validBacking);
		escrowService.getBackerOutstandingDTO(createValidUser(), otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerDTONull1() throws Exception {
		escrowService.getBackerDTO(null, otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerDTONull3() throws Exception {
		escrowService.getBackerDTO(createValidUser(), otherUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerDTOInvalid1() throws Exception {
		escrowService.getBackerDTO(createInvalidUser(), otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerDTOInvalid2() throws Exception {
		escrowService.getBackerDTO(createValidUser(), createInvalidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBackerDTOInvalid3() throws Exception {
		escrowService.getBackerDTO(createValidUser(), otherUser, invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void getBackerDTONotFound() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null);
		escrowService.getBackerDTO(createValidUser(), otherUser, validOtherName);
	}
	
	@Test
	public void getBackerDTO() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(validBacking);
		escrowService.getBackerDTO(createValidUser(), otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferDTONull1() throws Exception {
		escrowService.getOfferDTO(null, otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferDTONull3() throws Exception {
		escrowService.getOfferDTO(createValidUser(), otherUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferDTOInvalid1() throws Exception {
		escrowService.getOfferDTO(createInvalidUser(), otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferDTOInvalid2() throws Exception {
		escrowService.getOfferDTO(createValidUser(), createInvalidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferDTOInvalid3() throws Exception {
		escrowService.getOfferDTO(createValidUser(), otherUser, invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void getOfferDTONotFound() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null).thenReturn(null);
		escrowService.getOfferDTO(createValidUser(), otherUser, validOtherName);
	}
	
	@Test
	public void getOfferDTO() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(validOffer).thenReturn(null)
				.thenReturn(null).thenReturn(validEmailOffer)
				.thenReturn(validOffer).thenReturn(validEmailOffer);
		escrowService.getOfferDTO(createValidUser(), otherUser, validOtherName);
		escrowService.getOfferDTO(createValidUser(), null, validOtherName);
		escrowService.getOfferDTO(createValidUser(), otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferOutstandingDTONull1() throws Exception {
		escrowService.getOfferOutstandingDTO(null, otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferOutstandingDTONull3() throws Exception {
		escrowService.getOfferOutstandingDTO(createValidUser(), otherUser, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferOutstandingDTOInvalid1() throws Exception {
		escrowService.getOfferOutstandingDTO(createInvalidUser(), otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferOutstandingDTOInvalid2() throws Exception {
		escrowService.getOfferOutstandingDTO(createValidUser(), 
				createInvalidUser(), validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getOfferOutstandingDTOInvalid3() throws Exception {
		escrowService.getOfferOutstandingDTO(createValidUser(), otherUser, invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void getOfferOutstandingDTONotFound() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null);
		escrowService.getOfferOutstandingDTO(createValidUser(), otherUser, validOtherName);
	}
	
	@Test
	public void getOfferOutstandingDTO() throws Exception {
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(validBacking);
		escrowService.getOfferOutstandingDTO(createValidUser(), otherUser, validOtherName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getEmailOfferOutstandingDTONull1() throws Exception {
		escrowService.getEmailOfferOutstandingDTO(null, validEmail);
	}
	
	@Test(expected = BadParameterException.class)
	public void getEmailOfferOutstandingDTONull2() throws Exception {
		escrowService.getEmailOfferOutstandingDTO(createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getEmailOfferOutstandingDTOInvalid1() throws Exception {
		when(userService.findUserByUsername(anyString())).thenReturn(otherUser);
		when(userService.findUserByEmail(anyString())).thenReturn(otherUser);
		escrowService.getEmailOfferOutstandingDTO(createInvalidUser(), validEmail);
	}
	
	@Test(expected = BadParameterException.class)
	public void getEmailOfferOutstandingDTOInvalid2() throws Exception {
		when(userService.findUserByUsername(anyString())).thenReturn(otherUser);
		when(userService.findUserByEmail(anyString())).thenReturn(otherUser);
		escrowService.getEmailOfferOutstandingDTO(createValidUser(), invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void getEmailOfferOutstandingDTONotFound() throws Exception {
		when(userService.findUserByUsername(anyString())).thenReturn(otherUser);
		when(userService.findUserByEmail(anyString())).thenReturn(otherUser);
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(null);
		escrowService.getEmailOfferOutstandingDTO(createValidUser(), validEmail);
	}
	
	@Test
	public void getEmailOfferOutstandingDTO() throws Exception {
		when(userService.findUserByUsername(anyString())).thenReturn(otherUser);
		when(userService.findUserByEmail(anyString())).thenReturn(otherUser);
		when(escrowDao.findEscrow(any(ESCROW_TYPE.class), anyString(), anyString(), 
				anyString(), anyString())).thenReturn(validBacking);
		escrowService.getEmailOfferOutstandingDTO(createValidUser(), validEmail);
	}
	*/
}
