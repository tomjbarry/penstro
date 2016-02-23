package com.py.py.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.py.py.constants.CurrencyNames;
import com.py.py.dao.DealDao;
import com.py.py.dao.ReferenceDao;
import com.py.py.dao.WalletDao;
import com.py.py.domain.Deal;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.enumeration.DEAL_STATE;
import com.py.py.domain.enumeration.DEAL_TYPE;
import com.py.py.domain.enumeration.ESCROW_TYPE;
import com.py.py.domain.subdomain.EscrowSourceTarget;
import com.py.py.domain.subdomain.FinanceDescription;
import com.py.py.dto.in.PurchaseCurrencyDTO;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.BalanceException;
import com.py.py.service.exception.FinanceException;
import com.py.py.service.impl.FinanceServiceImpl;

public class FinanceServiceImplTest extends BaseServiceTest {

	@Autowired
	@Qualifier("financeService")
	private FinanceServiceImpl financeService;

	@Autowired
	private DealDao dealDao;
	
	@Autowired
	private WalletDao walletDao;
	
	@Autowired
	private ReferenceDao referenceDao;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private UserService userService;
	
	private String invalidCurrency = "blah39v-2390c";
	private String validCurrency = CurrencyNames.GOLD;
	private Deal validDeal = new Deal();
	private FinanceDescription validFinanceDescription = new FinanceDescription();
	private List<FinanceDescription> validTargets = new ArrayList<FinanceDescription>();
	private String invalidReferenceCollection = CollectionNames.USER;
	private EscrowSourceTarget validSourceTarget = new EscrowSourceTarget(
			(new ObjectId()).toHexString(),
			(new ObjectId()).toHexString(), 
			ESCROW_TYPE.BACKING);
	private ObjectId dealId = new ObjectId();
	//private ObjectId validTargetId = new ObjectId();
	//private List<ObjectId> validOtherIds = Arrays.asList(new ObjectId(), new ObjectId());
	
	@Before
	public void setUp() {
		reset(dealDao, walletDao, referenceDao, eventService, userService);
		
		validFinanceDescription.setAmount(1L);
		validFinanceDescription.setCurrency(validCurrency);
		validFinanceDescription.setUserId(new ObjectId());
		
		FinanceDescription fd1 = new FinanceDescription();
		fd1.setAmount(1L);
		fd1.setCurrency(validCurrency);
		fd1.setUserId(new ObjectId());
		
		validDeal.setPrimaryAmount(100l);
		validDeal.setSecondaryAmount(1L);
		validDeal.setCreated(new Date());
		validDeal.setCreateReferenceCost(true);
		validDeal.setId(dealId);
		validDeal.setReference(new ObjectId());
		validDeal.setReferenceAdded(true);
		validDeal.setReferenceCollection(CollectionNames.POSTING);
		validDeal.setSource(validFinanceDescription);
		validDeal.setSourceAdded(true);
		validDeal.setTargetsAdded(true);
		validDeal.setTargets(validTargets);
		validDeal.setState(DEAL_STATE.INITIAL);
		validDeal.setLastModified(new Date());
	}
	
	protected void successfulTransaction() throws Exception {
		when(walletDao.verifyHasFunds(anyString(), anyString(), anyLong(), anyString()))
			.thenReturn(true);
		when(dealDao.initializeDeal(any(DEAL_TYPE.class), any(FinanceDescription.class), 
				anyListOf(FinanceDescription.class), any(ObjectId.class), anyBoolean(), 
				anyString(), anyLong(), anyLong(), any(ObjectId.class)))
			.thenReturn(validDeal);
		when(dealDao.verifyDeal(any(ObjectId.class), any(DEAL_TYPE.class), 
				any(FinanceDescription.class), anyListOf(FinanceDescription.class), 
				any(ObjectId.class), anyBoolean(), anyString(), anyLong(), 
				anyLong(), any(ObjectId.class)))
			.thenReturn(true);
		when(dealDao.checkDealState(any(ObjectId.class), any(DEAL_STATE.class)))
			.thenReturn(true);
		when(walletDao.verifyTransactionStarted(anyString(), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(referenceDao.verifyTallyAdded(any(ObjectId.class), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(walletDao.verifyTransactionCompleted(anyString(), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(referenceDao.verifyTallyCompleted(any(ObjectId.class), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(referenceDao.verifyTallyCostCompleted(any(ObjectId.class), any(ObjectId.class), 
				anyLong(), anyString()))
			.thenReturn(true);
	}
	
	@Test(expected = BadParameterException.class)
	public void addCurrencyNull1() throws Exception {
		financeService.addCurrency(null, 1L);
	}
	@Test
	public void addCurrency() throws Exception {
		successfulTransaction();
		financeService.addCurrency(validUserId, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeCurrencyNull1() throws Exception {
		financeService.removeCurrency(null, 1L);
	}
	
	@Test
	public void removeCurrency() throws Exception {
		successfulTransaction();
		financeService.removeCurrency(validUserId, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void purchaseCurrencyNull1() throws Exception {
		PurchaseCurrencyDTO dto = new PurchaseCurrencyDTO();
		dto.setAmount(1L);
		financeService.purchaseCurrency(null, validUserId, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void purchaseCurrencyNull2() throws Exception {
		PurchaseCurrencyDTO dto = new PurchaseCurrencyDTO();
		dto.setAmount(1L);
		financeService.purchaseCurrency(validObjectId, null, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void purchaseCurrencyInvalid() throws Exception {
		PurchaseCurrencyDTO dto = new PurchaseCurrencyDTO();
		dto.setAmount(-1L);
		financeService.purchaseCurrency(validObjectId, validUserId, dto);
	}
	
	@Test
	public void purchaseCurrency() throws Exception {
		successfulTransaction();
		PurchaseCurrencyDTO dto = new PurchaseCurrencyDTO();
		dto.setAmount(1L);
		financeService.purchaseCurrency(validObjectId, validUserId, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void chargeUserNull1() throws Exception {
		financeService.charge((ObjectId)null, validObjectId, true, 
				CollectionNames.POSTING, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void chargeUserNull2() throws Exception {
		financeService.charge(validUserId, null, true, 
				CollectionNames.POSTING, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void chargeUserNull4() throws Exception {
		financeService.charge(validUserId, validObjectId, true, 
				null, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void chargeUserInvalid4() throws Exception {
		financeService.charge(validUserId, validObjectId, true, 
				invalidReferenceCollection, 1L);
	}
	
	@Test
	public void chargeUser() throws Exception {
		successfulTransaction();
		financeService.charge(validUserId, validObjectId, true, 
				CollectionNames.POSTING, 1L);
		financeService.charge(validUserId, validObjectId, false, 
				CollectionNames.POSTING, 1L);
		financeService.charge(validUserId, validObjectId, true, 
				CollectionNames.COMMENT, 1L);
		financeService.charge(validUserId, validObjectId, false, 
				CollectionNames.COMMENT, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void chargeEscrowNull1() throws Exception {
		financeService.charge((EscrowSourceTarget)null, validObjectId, true, 
				CollectionNames.POSTING, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void chargeEscrowNull2() throws Exception {
		financeService.charge(validSourceTarget, null, true, 
				CollectionNames.POSTING, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void chargeEscrowNull4() throws Exception {
		financeService.charge(validSourceTarget, validObjectId, true, 
				null, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void chargeEscrowInvalid4() throws Exception {
		financeService.charge(validSourceTarget, validObjectId, true, 
				invalidReferenceCollection, 1L);
	}
	
	@Test
	public void chargeEscrow() throws Exception {
		successfulTransaction();
		financeService.charge(validSourceTarget, validObjectId, true, 
				CollectionNames.POSTING, 1L);
		financeService.charge(validSourceTarget, validObjectId, false, 
				CollectionNames.POSTING, 1L);
		financeService.charge(validSourceTarget, validObjectId, true, 
				CollectionNames.COMMENT, 1L);
		financeService.charge(validSourceTarget, validObjectId, false, 
				CollectionNames.COMMENT, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void chargeForEscrowNull1() throws Exception {
		financeService.chargeForEscrow(null, validSourceTarget, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void chargeForEscrowNull2() throws Exception {
		financeService.chargeForEscrow(validUserId, null, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void chargeForEscrowInvalid() throws Exception {
		financeService.chargeForEscrow(validUserId, validSourceTarget, -2L);
	}
	
	@Test
	public void chargeForEscrow() throws Exception {
		successfulTransaction();
		financeService.chargeForEscrow(validUserId, validSourceTarget, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void refundEscrowNull1() throws Exception {
		financeService.refundEscrow(null, validUserId, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void refundEscrowNull2() throws Exception {
		financeService.refundEscrow(validSourceTarget, null, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void refundEscrowInvalid() throws Exception {
		financeService.refundEscrow(validSourceTarget, validUserId, -2L);
	}
	
	@Test
	public void refundEscrow() throws Exception {
		successfulTransaction();
		financeService.refundEscrow(validSourceTarget, validUserId, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void transferEscrowNull1() throws Exception {
		financeService.transferEscrow(null, validSourceTarget, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void transferEscrowNull2() throws Exception {
		financeService.transferEscrow(validSourceTarget, null, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void transferEscrowInvalid() throws Exception {
		financeService.transferEscrow(validSourceTarget, validSourceTarget, -2L);
	}
	
	@Test
	public void transferEscrow() throws Exception {
		successfulTransaction();
		financeService.transferEscrow(validSourceTarget, validSourceTarget, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void appreciateNull1() throws Exception {
		financeService.appreciate(null, validObjectId, CollectionNames.POSTING, 1L, 1L, 
				validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void appreciateNull2() throws Exception {
		financeService.appreciate(validUserId, null, CollectionNames.POSTING, 1L, 1L, 
				validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void appreciateNull3() throws Exception {
		financeService.appreciate(validUserId, validObjectId, null, 1L, 1L, validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void appreciateNull6() throws Exception {
		financeService.appreciate(validUserId, validObjectId, CollectionNames.POSTING, 
				1L, 1L, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void appreciateInvalid3() throws Exception {
		financeService.appreciate(validUserId, validObjectId, invalidReferenceCollection, 
				1L, 1L, validObjectId);
	}
	
	@Test
	public void appreciate() throws Exception {
		successfulTransaction();
		financeService.appreciate(validUserId, validObjectId, CollectionNames.POSTING, 
				1L, 1L, validObjectId);
		financeService.appreciate(validUserId, validObjectId, CollectionNames.COMMENT, 
				1L, 1L, validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void promoteNull1() throws Exception {
		financeService.promote(null, validObjectId, CollectionNames.POSTING, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void promoteNull2() throws Exception {
		financeService.promote(validUserId, null, CollectionNames.POSTING, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void promoteNull3() throws Exception {
		financeService.promote(validUserId, validObjectId, null, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void promoteInvalid3() throws Exception {
		financeService.promote(validUserId, validObjectId, invalidReferenceCollection, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void promoteInvalid4() throws Exception {
		financeService.promote(validUserId, validObjectId, CollectionNames.POSTING, -1L);
	}
	
	@Test
	public void promote() throws Exception {
		successfulTransaction();
		financeService.promote(validUserId, validObjectId, CollectionNames.POSTING, 1L);
		financeService.promote(validUserId, validObjectId, CollectionNames.COMMENT, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void transactionNull1() throws Exception {
		financeService.transaction(null, validFinanceDescription, 
				validTargets, validObjectId, true, CollectionNames.POSTING, 
				100l, 1L, validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void transactionNull3() throws Exception {
		FinanceDescription fd = new FinanceDescription();
		fd.setAmount(1L);
		fd.setCurrency(invalidCurrency);
		fd.setUserId(new ObjectId());
		financeService.transaction(DEAL_TYPE.APPRECIATE, validFinanceDescription, 
				Arrays.asList(fd), validObjectId, true, CollectionNames.POSTING, 
				100l, 1L, validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void transactionNull6() throws Exception {
		// exception due to providing a reference id but no collection
		financeService.transaction(DEAL_TYPE.APPRECIATE, validFinanceDescription, 
				validTargets, validObjectId, true, null, 100l, 1L, validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void transactionInvalid2() throws Exception {
		FinanceDescription fd = new FinanceDescription();
		fd.setAmount(1L);
		fd.setCurrency(invalidCurrency);
		fd.setUserId(new ObjectId());
		financeService.transaction(DEAL_TYPE.APPRECIATE, fd, 
				validTargets, validObjectId, true, CollectionNames.POSTING, 
				100l, 1L, validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void transactionInvalid6() throws Exception {
		FinanceDescription fd = new FinanceDescription();
		fd.setAmount(1L);
		fd.setCurrency(invalidCurrency);
		fd.setUserId(new ObjectId());
		financeService.transaction(DEAL_TYPE.APPRECIATE, validFinanceDescription, 
				validTargets, validObjectId, true, invalidReferenceCollection, 
				100l, 1L, validObjectId);
	}
	
	@Test(expected = BalanceException.class)
	public void transactionBalancePreInit() throws Exception {
		when(walletDao.verifyHasFunds(anyString(), anyString(), anyLong(), anyString()))
			.thenReturn(false);
		when(dealDao.initializeDeal(any(DEAL_TYPE.class), any(FinanceDescription.class), 
				anyListOf(FinanceDescription.class), any(ObjectId.class), anyBoolean(), 
				anyString(), anyLong(), anyLong(), any(ObjectId.class)))
			.thenReturn(validDeal);
		when(dealDao.verifyDeal(any(ObjectId.class), any(DEAL_TYPE.class), 
				any(FinanceDescription.class), anyListOf(FinanceDescription.class), 
				any(ObjectId.class), anyBoolean(), anyString(), anyLong(), 
				anyLong(), any(ObjectId.class)))
			.thenReturn(true);
		when(dealDao.checkDealState(any(ObjectId.class), any(DEAL_STATE.class)))
			.thenReturn(true);
		
		financeService.transaction(DEAL_TYPE.APPRECIATE, validFinanceDescription, 
				validTargets, validObjectId, true, CollectionNames.POSTING, 
				100l, 1L, validObjectId);
		// no cleanup, it was pre checked
	}
	
	@Test(expected = BalanceException.class)
	public void transactionBalancePostInit() throws Exception {
		when(walletDao.verifyHasFunds(anyString(), anyString(), anyLong(), anyString()))
			.thenReturn(true).thenReturn(false);
		when(dealDao.initializeDeal(any(DEAL_TYPE.class), any(FinanceDescription.class), 
				anyListOf(FinanceDescription.class), any(ObjectId.class), anyBoolean(), 
				anyString(), anyLong(), anyLong(), any(ObjectId.class)))
			.thenReturn(validDeal);
		when(dealDao.verifyDeal(any(ObjectId.class), any(DEAL_TYPE.class), 
				any(FinanceDescription.class), anyListOf(FinanceDescription.class), 
				any(ObjectId.class), anyBoolean(), anyString(), anyLong(), 
				anyLong(), any(ObjectId.class)))
			.thenReturn(true);
		when(dealDao.checkDealState(any(ObjectId.class), any(DEAL_STATE.class)))
			.thenReturn(true);
		when(walletDao.verifyTransactionStarted(anyString(), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(referenceDao.verifyTallyAdded(any(ObjectId.class), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(walletDao.verifyTransactionCompleted(anyString(), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(referenceDao.verifyTallyCompleted(any(ObjectId.class), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(referenceDao.verifyTallyCostCompleted(any(ObjectId.class), any(ObjectId.class), 
				anyLong(), anyString()))
			.thenReturn(true);
		
		financeService.transaction(DEAL_TYPE.APPRECIATE, validFinanceDescription, 
				validTargets, validObjectId, true, CollectionNames.POSTING, 
				100l, 1L, validObjectId);
		
		verify(dealDao).updateDealState(dealId, DEAL_STATE.FAILURE, null, null, null);
	}
	
	@Test(expected = FinanceException.class)
	public void transactionFinanceInit() throws Exception {
		when(walletDao.verifyHasFunds(anyString(), anyString(), anyLong(), anyString()))
			.thenReturn(true);
		when(dealDao.initializeDeal(any(DEAL_TYPE.class), any(FinanceDescription.class), 
				anyListOf(FinanceDescription.class), any(ObjectId.class), anyBoolean(), 
				anyString(), anyLong(), anyLong(), any(ObjectId.class)))
			.thenReturn(null);
		
		financeService.transaction(DEAL_TYPE.APPRECIATE, validFinanceDescription, 
				validTargets, validObjectId, true, CollectionNames.POSTING, 
				100l, 1L, validObjectId);
		
		verify(dealDao).updateDealState(dealId, DEAL_STATE.FAILURE, null, null, null);
	}
	
	@Test(expected = FinanceException.class)
	public void transactionFinanceVerifyDeal() throws Exception {
		when(walletDao.verifyHasFunds(anyString(), anyString(), anyLong(), anyString()))
			.thenReturn(true);
		when(dealDao.initializeDeal(any(DEAL_TYPE.class), any(FinanceDescription.class), 
				anyListOf(FinanceDescription.class), any(ObjectId.class), anyBoolean(), 
				anyString(), anyLong(), anyLong(), any(ObjectId.class)))
			.thenReturn(validDeal);
		when(dealDao.verifyDeal(any(ObjectId.class), any(DEAL_TYPE.class), 
				any(FinanceDescription.class), anyListOf(FinanceDescription.class), 
				any(ObjectId.class), anyBoolean(), anyString(), anyLong(), 
				anyLong(), any(ObjectId.class)))
			.thenReturn(false);
		
		financeService.transaction(DEAL_TYPE.APPRECIATE, validFinanceDescription, 
				validTargets, validObjectId, true, CollectionNames.POSTING, 
				100l, 1L, validObjectId);
		
		verify(dealDao).updateDealState(dealId, DEAL_STATE.FAILURE, null, null, null);
	}
	
	@Test(expected = FinanceException.class)
	public void transactionFinanceVerifyDealState() throws Exception {
		when(walletDao.verifyHasFunds(anyString(), anyString(), anyLong(), anyString()))
			.thenReturn(true).thenReturn(false);
		when(dealDao.initializeDeal(any(DEAL_TYPE.class), any(FinanceDescription.class), 
				anyListOf(FinanceDescription.class), any(ObjectId.class), anyBoolean(), 
				anyString(), anyLong(), anyLong(), any(ObjectId.class)))
			.thenReturn(validDeal);
		when(dealDao.verifyDeal(any(ObjectId.class), any(DEAL_TYPE.class), 
				any(FinanceDescription.class), anyListOf(FinanceDescription.class), 
				any(ObjectId.class), anyBoolean(), anyString(), anyLong(), 
				anyLong(), any(ObjectId.class)))
			.thenReturn(true);
		when(dealDao.checkDealState(any(ObjectId.class), any(DEAL_STATE.class)))
			.thenReturn(false);
		
		financeService.transaction(DEAL_TYPE.APPRECIATE, validFinanceDescription, 
				validTargets, validObjectId, true, CollectionNames.POSTING, 
				100l, 1L, validObjectId);
		
		verify(dealDao).updateDealState(dealId, DEAL_STATE.FAILURE, null, null, null);
	}
	
	@Test(expected = FinanceException.class)
	public void transactionFinanceVerifyStarted() throws Exception {
		when(walletDao.verifyHasFunds(anyString(), anyString(), anyLong(), anyString()))
			.thenReturn(true).thenReturn(false);
		when(dealDao.initializeDeal(any(DEAL_TYPE.class), any(FinanceDescription.class), 
				anyListOf(FinanceDescription.class), any(ObjectId.class), anyBoolean(), 
				anyString(), anyLong(), anyLong(), any(ObjectId.class)))
			.thenReturn(validDeal);
		when(dealDao.verifyDeal(any(ObjectId.class), any(DEAL_TYPE.class), 
				any(FinanceDescription.class), anyListOf(FinanceDescription.class), 
				any(ObjectId.class), anyBoolean(), anyString(), anyLong(), 
				anyLong(), any(ObjectId.class)))
			.thenReturn(true);
		when(dealDao.checkDealState(any(ObjectId.class), any(DEAL_STATE.class)))
			.thenReturn(true);
		when(walletDao.verifyTransactionStarted(anyString(), any(ObjectId.class), 
				anyString()))
			.thenReturn(false);
		when(referenceDao.verifyTallyAdded(any(ObjectId.class), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		
		financeService.transaction(DEAL_TYPE.APPRECIATE, validFinanceDescription, 
				validTargets, validObjectId, true, CollectionNames.POSTING, 
				100l, 1L, validObjectId);
		
		verify(dealDao).updateDealState(dealId, DEAL_STATE.FAILURE, null, null, null);
	}
	
	@Test(expected = FinanceException.class)
	public void transactionVerifyTallyAdded() throws Exception {
		when(walletDao.verifyHasFunds(anyString(), anyString(), anyLong(), anyString()))
			.thenReturn(true).thenReturn(false);
		when(dealDao.initializeDeal(any(DEAL_TYPE.class), any(FinanceDescription.class), 
				anyListOf(FinanceDescription.class), any(ObjectId.class), anyBoolean(), 
				anyString(), anyLong(), anyLong(), any(ObjectId.class)))
			.thenReturn(validDeal);
		when(dealDao.verifyDeal(any(ObjectId.class), any(DEAL_TYPE.class), 
				any(FinanceDescription.class), anyListOf(FinanceDescription.class), 
				any(ObjectId.class), anyBoolean(), anyString(), anyLong(), 
				anyLong(), any(ObjectId.class)))
			.thenReturn(true);
		when(dealDao.checkDealState(any(ObjectId.class), any(DEAL_STATE.class)))
			.thenReturn(true);
		when(walletDao.verifyTransactionStarted(anyString(), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(referenceDao.verifyTallyAdded(any(ObjectId.class), any(ObjectId.class), 
				anyString()))
			.thenReturn(false);
		
		financeService.transaction(DEAL_TYPE.APPRECIATE, validFinanceDescription, 
				validTargets, validObjectId, true, CollectionNames.POSTING, 
				100l, 1L, validObjectId);
		
		verify(dealDao).updateDealState(dealId, DEAL_STATE.FAILURE, null, null, null);
	}
	
	@Test(expected = FinanceException.class)
	public void transactionFinanceVerifyCompleted() throws Exception {
		when(walletDao.verifyHasFunds(anyString(), anyString(), anyLong(), anyString()))
			.thenReturn(true).thenReturn(false);
		when(dealDao.initializeDeal(any(DEAL_TYPE.class), any(FinanceDescription.class), 
				anyListOf(FinanceDescription.class), any(ObjectId.class), anyBoolean(), 
				anyString(), anyLong(), anyLong(), any(ObjectId.class)))
			.thenReturn(validDeal);
		when(dealDao.verifyDeal(any(ObjectId.class), any(DEAL_TYPE.class), 
				any(FinanceDescription.class), anyListOf(FinanceDescription.class), 
				any(ObjectId.class), anyBoolean(), anyString(), anyLong(), 
				anyLong(), any(ObjectId.class)))
			.thenReturn(true);
		when(dealDao.checkDealState(any(ObjectId.class), any(DEAL_STATE.class)))
			.thenReturn(true);
		when(walletDao.verifyTransactionStarted(anyString(), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(referenceDao.verifyTallyAdded(any(ObjectId.class), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(walletDao.verifyTransactionCompleted(anyString(), any(ObjectId.class), 
				anyString()))
			.thenReturn(false);
		
		financeService.transaction(DEAL_TYPE.APPRECIATE, validFinanceDescription, 
				validTargets, validObjectId, true, CollectionNames.POSTING, 
				100l, 1L, validObjectId);
		
		verify(dealDao).updateDealState(dealId, DEAL_STATE.FAILURE, null, null, null);
	}
	
	@Test(expected = FinanceException.class)
	public void transactionFinanceVerifyTallyCompleted() throws Exception {
		when(walletDao.verifyHasFunds(anyString(), anyString(), anyLong(), anyString()))
			.thenReturn(true).thenReturn(false);
		when(dealDao.initializeDeal(any(DEAL_TYPE.class), any(FinanceDescription.class), 
				anyListOf(FinanceDescription.class), any(ObjectId.class), anyBoolean(), 
				anyString(), anyLong(), anyLong(), any(ObjectId.class)))
			.thenReturn(validDeal);
		when(dealDao.verifyDeal(any(ObjectId.class), any(DEAL_TYPE.class), 
				any(FinanceDescription.class), anyListOf(FinanceDescription.class), 
				any(ObjectId.class), anyBoolean(), anyString(), anyLong(), 
				anyLong(), any(ObjectId.class)))
			.thenReturn(true);
		when(dealDao.checkDealState(any(ObjectId.class), any(DEAL_STATE.class)))
			.thenReturn(true);
		when(walletDao.verifyTransactionStarted(anyString(), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(referenceDao.verifyTallyAdded(any(ObjectId.class), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(walletDao.verifyTransactionCompleted(anyString(), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(referenceDao.verifyTallyCompleted(any(ObjectId.class), any(ObjectId.class), 
				anyString()))
			.thenReturn(false);
		
		financeService.transaction(DEAL_TYPE.APPRECIATE, validFinanceDescription, 
				validTargets, validObjectId, false, CollectionNames.POSTING, 
				100l, 1L, validObjectId);
		
		verify(dealDao).updateDealState(dealId, DEAL_STATE.FAILURE, null, null, null);
	}
	
	@Test(expected = FinanceException.class)
	public void transactionFinanceVerifyTallyCostCompleted() throws Exception {
		when(walletDao.verifyHasFunds(anyString(), anyString(), anyLong(), anyString()))
			.thenReturn(true).thenReturn(false);
		when(dealDao.initializeDeal(any(DEAL_TYPE.class), any(FinanceDescription.class), 
				anyListOf(FinanceDescription.class), any(ObjectId.class), anyBoolean(), 
				anyString(), anyLong(), anyLong(), any(ObjectId.class)))
			.thenReturn(validDeal);
		when(dealDao.verifyDeal(any(ObjectId.class), any(DEAL_TYPE.class), 
				any(FinanceDescription.class), anyListOf(FinanceDescription.class), 
				any(ObjectId.class), anyBoolean(), anyString(), anyLong(), 
				anyLong(), any(ObjectId.class)))
			.thenReturn(true);
		when(dealDao.checkDealState(any(ObjectId.class), any(DEAL_STATE.class)))
			.thenReturn(true);
		when(walletDao.verifyTransactionStarted(anyString(), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(referenceDao.verifyTallyAdded(any(ObjectId.class), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(walletDao.verifyTransactionCompleted(anyString(), any(ObjectId.class), 
				anyString()))
			.thenReturn(true);
		when(referenceDao.verifyTallyCostCompleted(any(ObjectId.class), any(ObjectId.class), 
				anyLong(), anyString()))
			.thenReturn(false);
		
		financeService.transaction(DEAL_TYPE.APPRECIATE, validFinanceDescription, 
				validTargets, validObjectId, true, CollectionNames.POSTING, 
				100l, 1L, validObjectId);
		
		verify(dealDao).updateDealState(dealId, DEAL_STATE.FAILURE, null, null, null);
	}
	
	@Test
	public void transaction() throws Exception {
		successfulTransaction();
		financeService.transaction(DEAL_TYPE.APPRECIATE, validFinanceDescription, 
				validTargets, validObjectId, true, CollectionNames.POSTING, 
				100l, 1L, validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void cleanupDealNull() throws Exception {
		financeService.cleanupDeal(null);
	}
	
	@Test(expected = BadParameterException.class)
	public void cleanupDealInvalid() throws Exception {
		Deal deal = new Deal();
		deal.setId(null);
		financeService.cleanupDeal(deal);
	}
	
	@Test
	public void cleanupDealValidSource() throws Exception {
		Deal deal = new Deal();
		deal.setId(dealId);
		deal.setSource(validFinanceDescription);
		deal.setSourceAdded(true);
		String id = FinanceDescription.getId(validFinanceDescription);
		String currency = validFinanceDescription.getCurrency();
		long amount = validFinanceDescription.getAmount();
		String collectionName = FinanceDescription.getCollection(validFinanceDescription);
		
		financeService.cleanupDeal(deal);
		verify(walletDao).revertPendingTransaction(id, dealId, currency, amount, 
				collectionName);
	}
	
	@Test
	public void cleanupDealValidTarget() throws Exception {
		Deal deal = new Deal();
		deal.setId(dealId);
		deal.setTargets(Arrays.asList(validFinanceDescription));
		deal.setTargetsAdded(true);
		String id = FinanceDescription.getId(validFinanceDescription);
		String currency = validFinanceDescription.getCurrency();
		long amount = 0 - validFinanceDescription.getAmount();
		String collectionName = FinanceDescription.getCollection(validFinanceDescription);
		
		financeService.cleanupDeal(deal);
		verify(walletDao).revertPendingTransaction(id, dealId, currency, amount, 
				collectionName);
	}
	
	@Test
	public void cleanupDealValidReferenceTallyPosting() throws Exception {
		Deal deal = new Deal();
		deal.setId(dealId);
		deal.setReference(validObjectId);
		deal.setPrimaryAmount(500l);
		deal.setSecondaryAmount(5L);
		deal.setCreateReferenceCost(false);
		deal.setReferenceAdded(true);
		String collectionName = CollectionNames.POSTING;
		deal.setReferenceCollection(collectionName);
		long primaryAmount = 0 - 500l;
		long secondaryAmount = 0 - 5L;
		
		financeService.cleanupDeal(deal);
		verify(referenceDao).revertPendingTally(validObjectId, dealId, primaryAmount, 
				secondaryAmount, collectionName);
	}
	
	@Test
	public void cleanupDealValidReferenceTallyComment() throws Exception {
		Deal deal = new Deal();
		deal.setId(dealId);
		deal.setReference(validObjectId);
		deal.setPrimaryAmount(500l);
		deal.setSecondaryAmount(5L);
		deal.setCreateReferenceCost(false);
		deal.setReferenceAdded(true);
		String collectionName = CollectionNames.COMMENT;
		deal.setReferenceCollection(collectionName);
		long primaryAmount = 0 - 500l;
		long secondaryAmount = 0 - 5L;
		
		financeService.cleanupDeal(deal);
		verify(referenceDao).revertPendingTally(validObjectId, dealId, primaryAmount, 
				secondaryAmount, collectionName);
	}
	
	@Test
	public void cleanupDealValidReferenceCostPosting() throws Exception {
		Deal deal = new Deal();
		deal.setId(dealId);
		deal.setReference(validObjectId);
		deal.setPrimaryAmount(500l);
		deal.setSecondaryAmount(5L);
		deal.setCreateReferenceCost(true);
		deal.setReferenceAdded(true);
		String collectionName = CollectionNames.POSTING;
		deal.setReferenceCollection(collectionName);
		
		financeService.cleanupDeal(deal);
		verify(referenceDao).revertPendingCost(validObjectId, dealId, collectionName);
	}
	
	@Test
	public void cleanupDealValidReferenceCostComment() throws Exception {
		Deal deal = new Deal();
		deal.setId(dealId);
		deal.setReference(validObjectId);
		deal.setPrimaryAmount(500l);
		deal.setSecondaryAmount(5L);
		deal.setCreateReferenceCost(true);
		deal.setReferenceAdded(true);
		String collectionName = CollectionNames.COMMENT;
		deal.setReferenceCollection(collectionName);
		
		financeService.cleanupDeal(deal);
		verify(referenceDao).revertPendingCost(validObjectId, dealId, collectionName);
	}
	
	@Test
	public void cleanupDeal() throws Exception {
		financeService.cleanupDeal(validDeal);
		verify(dealDao).updateDealState(dealId, DEAL_STATE.PENDING_FAILURE, 
				null, null, null);
		verify(dealDao).updateDealState(dealId, DEAL_STATE.FAILURE, null, null, null);
	}
}
