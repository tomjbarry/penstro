package com.py.py.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.util.UriComponentsBuilder;

import com.paypal.ipn.IPNMessage;
import com.paypal.svcs.services.AdaptivePaymentsService;
import com.paypal.svcs.types.ap.PayRequest;
import com.paypal.svcs.types.ap.PayResponse;
import com.paypal.svcs.types.ap.PaymentDetailsRequest;
import com.paypal.svcs.types.ap.PaymentDetailsResponse;
import com.paypal.svcs.types.ap.Receiver;
import com.paypal.svcs.types.ap.ReceiverList;
import com.paypal.svcs.types.common.AckCode;
import com.paypal.svcs.types.common.ClientDetailsType;
import com.paypal.svcs.types.common.RequestEnvelope;
import com.paypal.svcs.types.common.ResponseEnvelope;
import com.py.py.constants.ParamNames;
import com.py.py.constants.ServiceValues;
import com.py.py.dao.PaymentDao;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Comment;
import com.py.py.domain.Payment;
import com.py.py.domain.Posting;
import com.py.py.domain.User;
import com.py.py.domain.enumeration.PAYMENT_MARK;
import com.py.py.domain.enumeration.PAYMENT_STATE;
import com.py.py.domain.enumeration.PAYMENT_TYPE;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.dto.DTO;
import com.py.py.dto.in.AppreciateCommentDTO;
import com.py.py.dto.in.AppreciatePostingDTO;
import com.py.py.dto.in.PurchaseCurrencyDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.service.CommentService;
import com.py.py.service.EventService;
import com.py.py.service.FinanceService;
import com.py.py.service.PaymentService;
import com.py.py.service.PostingService;
import com.py.py.service.UserService;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.ExternalServiceException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.PaymentException;
import com.py.py.service.exception.PaymentNotificationException;
import com.py.py.service.exception.PaymentTargetException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.util.PyLogger;
import com.py.py.util.PyUtils;

public class PaymentServiceImpl implements PaymentService {
	
	protected static final PyLogger logger = PyLogger.getLogger(PaymentServiceImpl.class);

	protected final String ACTION_TYPE = "PAY";
	protected final String CURRENCY_CODE = "USD";

	protected final String DIGITALGOODS = "DIGITALGOODS";
	protected final String EACHRECEIVER = "EACHRECEIVER";
	protected final String FORMATTED_PAYKEY_DURATION = 
			"PT" + ServiceValues.PAYKEY_DURATION_HOURS + "H";
	
	protected AdaptivePaymentsService ppAps;
	protected RequestEnvelope requestEnvelope = new RequestEnvelope();
	protected String selfPaymentId;
	protected String redirectPaymentUrl;
	protected String ipnUrl;
	protected Map<String, String> ipnConfig = new HashMap<String, String>();
	protected String appName;
	protected String purchaseMemo;
	protected String appreciationMemo;
	protected String appreciationMemoAlt;
	protected String success;
	protected String cancel;
	
	@Autowired
	protected PaymentDao paymentDao;

	@Autowired
	protected UserService userService;
	
	@Autowired
	protected PostingService postingService;
	
	@Autowired
	protected CommentService commentService;
	
	@Autowired
	protected FinanceService financeService;
	
	@Autowired
	protected EventService eventService;
	
	protected BigDecimal getPromotionFee(BigDecimal amount) {
		if(amount == null || amount.compareTo(PyUtils.formatBDDown(0.0d)) <= 0) {
			return PyUtils.formatBDDown(0.0d);
		}
		return PyUtils.formatBDHalfUp(amount.multiply(new BigDecimal(ServiceValues.PAYMENT_APPRECIATION_FEE)));
	}
	
	protected PayRequest constructMultiPayRequest(List<Receiver> receiverList, 
			String ipAddress, String successUrl, String cancelUrl) {
		PayRequest payRequest = constructPayRequest(receiverList, ipAddress, successUrl, cancelUrl);
		payRequest.setReverseAllParallelPaymentsOnError(true);
		return payRequest;
	}
	
	protected PayRequest constructPayRequest(List<Receiver> receiverList, 
			String ipAddress, String successUrl, String cancelUrl) {
		ReceiverList rl = new ReceiverList(receiverList);
		PayRequest payRequest =  new PayRequest(requestEnvelope, ACTION_TYPE, cancelUrl, 
				CURRENCY_CODE, rl, successUrl);

		// tracking id added later
		payRequest.setFeesPayer(EACHRECEIVER);
		ClientDetailsType clientDetails = new ClientDetailsType();
		clientDetails.setIpAddress(ipAddress);
		clientDetails.setApplicationId(appName);
		payRequest.setPayKeyDuration(FORMATTED_PAYKEY_DURATION);
		payRequest.setClientDetails(clientDetails);
		if(ipnUrl != null) {
			payRequest.setIpnNotificationUrl(ipnUrl);
		}
		return payRequest;
	}
	
	protected Receiver getChargeReceiver(double amount) {
		Receiver receiver = constructReceiver(amount, false, selfPaymentId);
		receiver.setPaymentType(DIGITALGOODS);
		return receiver;
	}
	
	protected Receiver constructReceiver(double amount, boolean primary, String paymentId) {
		Receiver receiver = new Receiver(amount);
		receiver.setEmail(paymentId);
		receiver.setPrimary(primary);
		receiver.setPaymentType(DIGITALGOODS);
		return receiver;
	}
	
	public PaymentServiceImpl(Properties properties, String redirectPaymentUrl, 
			String selfPaymentId, String ipnUrl, Map<String, String> ipnConfig, String appName, String purchaseMemo, String appreciationMemo,
			String appreciationMemoAlt, String success, String cancel) {
		ppAps = new AdaptivePaymentsService(properties);
		requestEnvelope.setErrorLanguage("en_US");
		this.redirectPaymentUrl = redirectPaymentUrl;
		this.selfPaymentId = selfPaymentId;
		this.ipnUrl = ipnUrl;
		this.ipnConfig = ipnConfig;
		this.appName = appName;
		this.purchaseMemo = purchaseMemo;
		this.appreciationMemo = appreciationMemo;
		this.appreciationMemoAlt = appreciationMemoAlt;
		this.success = success;
		this.cancel = cancel;
		// purely to throw an error and alert if the urls are malformed
		try {
			UriComponentsBuilder.fromUriString(redirectPaymentUrl).build().toUriString();
			UriComponentsBuilder.fromUriString(ipnUrl).build().toUriString();
			UriComponentsBuilder.fromUriString(success).build().toUriString();
			UriComponentsBuilder.fromUriString(cancel).build().toUriString();
		} catch(IllegalArgumentException iae) {
			logger.warn("Exception checking for malformed URI strings!", iae);
			throw iae;
		}
	}
	
	protected Payment getPayment(ObjectId id, String payKey) throws ServiceException {
		if(id == null && (payKey == null || payKey.isEmpty())) {
			throw new BadParameterException();
		}

		if(payKey != null && payKey.isEmpty()) {
			payKey = null;
		}
		
		try {
			Payment payment = paymentDao.findPayment(id, payKey);
			if(payment == null) {
				throw new NotFoundException(id.toHexString());
			}
			return payment;
		} catch(NotFoundException nfe) {
			throw nfe;
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public void checkPayment(ObjectId userId, ObjectId id, String payKey) throws ServiceException {
		if(id == null && (payKey == null || payKey.isEmpty())) {
			throw new BadParameterException();
		}
		try {
			if(payKey != null && payKey.isEmpty()) {
				payKey = null;
			}
			Payment payment = getPayment(id, payKey);
			
			if(!PyUtils.objectIdCompare(userId, payment.getSourceId())) {
				throw new ActionNotAllowedException();
			}
			
			// if its complete, return without exception! otherwise, throw exception
			if(payment.getState() == PAYMENT_STATE.COMPLETED) {
				return;
			}
			throw new ActionNotAllowedException();
		} catch(ActionNotAllowedException anae) {
			throw anae;
		} catch(NotFoundException nfe) {
			throw new ActionNotAllowedException();
		} catch(Exception e) {
			logger.info("Exception while checking payment status for id {"
					+ id + "} with payKey '" + payKey + "'.", e);
			throw new ServiceException();
		}
	}
	
	@Override
	public void paymentNotification(HttpServletRequest request) throws ServiceException {
		if(request == null) {
			throw new PaymentNotificationException();
		}
		try {
			IPNMessage ipnlistener = new IPNMessage(request, ipnConfig);
			boolean isIpnVerified = ipnlistener.validate();
			//String transactionType = ipnlistener.getTransactionType();
			Map<String, String> map = ipnlistener.getIpnMap();
			
			if(isIpnVerified && PyUtils.stringCompare("COMPLETED", map.get("status"))) {
				ObjectId oid = new ObjectId(map.get("tracking_id"));
				String payKey = map.get("pay_key");
				approvePayment(oid, payKey);
			}
		} catch(Exception e) {
			throw new PaymentNotificationException();
		}
		return;
		
	}
	
	protected void approvePayment(ObjectId id, String payKey) throws ServiceException {
		if(id == null && (payKey == null || payKey.isEmpty())) {
			throw new BadParameterException();
		}
		// user should have no knowledge of payment status, this is just to immediately
		// update its purpose
		try {
			if(payKey != null && payKey.isEmpty()) {
				payKey = null;
			}
			Payment payment = null;
			try {
				payment = getPayment(id, payKey);
				if(payment == null) {
					throw new NotFoundException(payKey);
				}
			} catch(NotFoundException nfe) {
				throw new ActionNotAllowedException();
			}
			
			// if its not complete, mark, otherwise return without exception
			if(payment.getState() == PAYMENT_STATE.COMPLETED) {
				return;
			} else if(payment.getState() != PAYMENT_STATE.FAILURE
				&& payment.getState() != PAYMENT_STATE.CANCELLED
				&& payment.getState() != PAYMENT_STATE.COMPLETION_FAILURE) {
				// filter out payments that have completed or failed, and add any other to a list 
				// to ensure it is checked on the next background task
				paymentDao.markPayment(payment.getId(), null, PAYMENT_MARK.APPROVED);
				return;
			}
			throw new NotFoundException(payment.getPayKey());
		} catch(NotFoundException nfe) {
			throw nfe;
		} catch(DaoException de) {
			logger.info("Exception while payKey payment status for id {"
					+ id + "} with payKey '" + payKey + "'.", de);
			throw new ServiceException(de);
		} catch(Exception e) {
			logger.info("Exception while approving payment status for id {"
					+ id + "} with payKey '" + payKey + "'.", e);
			throw new ServiceException();
		}
	}
	
	@Override
	public void markPayment(ObjectId userId, ObjectId id, String payKey) throws ServiceException {
		ArgCheck.nullCheck(userId);
		if(id == null && (payKey == null || payKey.isEmpty())) {
			throw new BadParameterException();
		}
		// user should have no knowledge of payment status, this is just to immediately
		// update its purpose
		try {
			if(payKey != null && payKey.isEmpty()) {
				payKey = null;
			}
			Payment payment = null;
			try {
				payment = getPayment(id, payKey);
				if(payment == null) {
					throw new NotFoundException(payKey);
				}
			} catch(NotFoundException nfe) {
				throw new ActionNotAllowedException();
			}
			
			if(!PyUtils.objectIdCompare(userId, payment.getSourceId())) {
				throw new ActionNotAllowedException();
			}
			
			// if its not complete, mark, otherwise return without exception
			if(payment.getState() == PAYMENT_STATE.COMPLETED) {
				return;
			} else if(payment.getState() != PAYMENT_STATE.FAILURE
				&& payment.getState() != PAYMENT_STATE.CANCELLED
				&& payment.getState() != PAYMENT_STATE.COMPLETION_FAILURE) {
				// filter out payments that have completed or failed, and add any other to a list 
				// to ensure it is checked on the next background task
				paymentDao.markPayment(payment.getId(), null, PAYMENT_MARK.CHECK);
				return;
			}
			throw new NotFoundException(payment.getPayKey());
		} catch(NotFoundException nfe) {
			throw nfe;
		} catch(DaoException de) {
			logger.info("Exception while checking payment status for id {"
					+ id + "} with payKey '" + payKey + "'.", de);
			throw new ServiceException(de);
		} catch(Exception e) {
			logger.info("Exception while checking payment status for id {"
					+ id + "} with payKey '" + payKey + "'.", e);
			throw new ServiceException();
		}
	}
	
	protected void checkPaymentStatus(Payment payment) throws ServiceException {
		ArgCheck.nullCheck(payment);
		// really should be unnecessary
		ArgCheck.nullCheck(payment.getId());
		
		if(payment.getState() == PAYMENT_STATE.COMPLETED) {
			// these have already been checked
			return;
		}
		if(payment.getState() == PAYMENT_STATE.FAILURE
				|| payment.getState() == PAYMENT_STATE.CANCELLED) {
			// do not throw exception, this can happen for marked payments
			return;
		}
		
		if(payment.getState() == PAYMENT_STATE.APPROVED
				|| payment.getState() == PAYMENT_STATE.COMPLETION_ERROR) {
			completePayment(payment);
			return;
		}
		
		PaymentDetailsRequest paymentDetailsRequest = 
				new PaymentDetailsRequest(requestEnvelope);
		
		if(payment.getPayKey() != null) {
			paymentDetailsRequest.setPayKey(payment.getPayKey());
		} else {
			paymentDetailsRequest.setTrackingId(payment.getId().toHexString());
		}
		
		PaymentDetailsResponse response = null;
		try {
			response = ppAps.paymentDetails(paymentDetailsRequest);
			if(response == null) {
				logger.warn("Response from paypal while checking payment status was not found!");
				throw new ExternalServiceException();
			}
		} catch(Exception e) {
			logger.warn("Exception connecting to paypal while checking payment status!", e);
			throw new ExternalServiceException();
		}
		if(!responseEnvelopeSuccessful(response.getResponseEnvelope())) {
			logger.warn("Response was unsuccessful from paypal!");
			// separate exception because paypal service is assumed to be working, but
			// some error is causing this to be unsuccessful
			// One possibility is that paypal never received the payment information, and
			// the document was not updated as such
			// DO NOT FAIL PAYMENT! TRY AGAIN LATER!
			throw new PaymentException();
		}
		
		String status = response.getStatus();
		if(status == null) {
			// DO NOT FAIL PAYMENT! TRY AGAIN LATER!
			throw new PaymentException();
		}
		
		if(payment.getPayKey() == null && response.getPayKey() != null) {
			try {
				paymentDao.setPayKey(payment.getId(), payment.getState(), 
						response.getPayKey());
			} catch(DaoException de) {
				logger.info("PayKey for payment with id {" + payment.getId() + "} was not set!", de);
			}
		}
		
		if(EXEC_STATUS.CREATED.toString().equalsIgnoreCase(status)) {
			// do nothing
			throw new ActionNotAllowedException();
		} else if(EXEC_STATUS.COMPLETED.toString().equalsIgnoreCase(status)) {
			completePayment(payment);
		} else if(EXEC_STATUS.EXPIRED.toString().equalsIgnoreCase(status)) {
			cancelPayment(payment);
		} else if(EXEC_STATUS.PROCESSING.toString().equalsIgnoreCase(status)) {
			// do nothing, it is still in progress
			throw new ActionNotAllowedException();
		} else if(EXEC_STATUS.PENDING.toString().equalsIgnoreCase(status)) {
			// do nothing, it is still in progress
			throw new ActionNotAllowedException();
		} else if(EXEC_STATUS.INCOMPLETE.toString().equalsIgnoreCase(status)) {
			// do nothing, it is still in progress
			throw new ActionNotAllowedException();
		} else {
			// DO NOT FAIL PAYMENT! TRY AGAIN LATER
			throw new PaymentException();
		}
	}
	
	protected void checkApprovedPaymentStatus(Payment payment) throws ServiceException {
		ArgCheck.nullCheck(payment);
		// really should be unnecessary
		ArgCheck.nullCheck(payment.getId());
		
		if(payment.getState() == PAYMENT_STATE.COMPLETED) {
			// these have already been checked
			return;
		}
		if(payment.getState() == PAYMENT_STATE.FAILURE
				|| payment.getState() == PAYMENT_STATE.CANCELLED) {
			// do not throw exception, although this is probably an error on our end... lets log it
			logger.info("Approved payment {" + payment.getId().toHexString() + "} was failed or cancelled.");
			return;
		}
		
		completePayment(payment);
	}
	
	@Override
	public ResultSuccessDTO purchaseCurrency(User user, PurchaseCurrencyDTO dto, 
			String ipAddress) throws ServiceException {
		ArgCheck.nullCheck(dto, ipAddress);
		ArgCheck.userCheck(user);
		ArgCheck.nullCheck(user.getId(), user.getUsername());
		
		// calculation of fees
		BigDecimal pretax = PyUtils.formatBDDown(financeService.getCurrencyCost(dto.getAmount()));
		//BigDecimal amountBd = new BigDecimal(Double.toString(currencyCost));
		//amountBd = amountBd.setScale(ServiceValues.PAYMENT_DOUBLE_PRECISION, 
		//		BigDecimal.ROUND_FLOOR);
		//double correctAmount = amountBd.doubleValue();
		BigDecimal correctAmount = PyUtils.formatBDDown(pretax.add(financeService.getTaxFromCost(pretax)));
		
		List<Receiver> receiverList = new ArrayList<Receiver>();
		receiverList.add(getChargeReceiver(correctAmount.doubleValue()));
		
		PayRequest payRequest = constructPayRequest(receiverList, ipAddress, success, cancel);
		payRequest.setMemo(purchaseMemo);
		
		String resultUrl = payment(payRequest, PAYMENT_TYPE.PURCHASE_CURRENCY, null, 
				user.getId(), null, null, null, correctAmount, dto, success, cancel);
		
		logger.debug("Currency purchased by user (" + user.getUsername() + ") with id {" 
				+ user.getId().toHexString() + "}.");
		
		ResultSuccessDTO result = new ResultSuccessDTO();
		result.setResult(resultUrl);
		return result;
	}
	
	@Override
	public ResultSuccessDTO adminAppreciatePosting(User user, Posting posting, 
			AppreciatePostingDTO dto, String ipAddress)
					throws ServiceException {
		ArgCheck.nullCheck(posting, dto, ipAddress);
		ArgCheck.userCheck(user);
		List<CachedUsername> beneficiaries = new ArrayList<CachedUsername>();
		if(posting.getBeneficiary() != null) {
			beneficiaries.add(posting.getBeneficiary());
		}
		
		if(!postingService.canAppreciate(posting)) {
			throw new ActionNotAllowedException();
		}
		
		return adminAppreciate(user, posting.getId(), posting.getAuthor(), 
				beneficiaries, PyUtils.convertBDFromString(dto.getAppreciation()), dto, 
				PAYMENT_TYPE.APPRECIATE_POSTING, ipAddress, success, cancel);
	}
	
	@Override
	public ResultSuccessDTO adminAppreciateComment(User user, Comment comment, 
			AppreciateCommentDTO dto, String ipAddress)
					throws ServiceException {
		ArgCheck.nullCheck(comment, dto, ipAddress);
		ArgCheck.userCheck(user);
		List<CachedUsername> beneficiaries = new ArrayList<CachedUsername>();
		if(comment.getBeneficiary() != null) {
			beneficiaries.add(comment.getBeneficiary());
		}
		
		if(!commentService.canAppreciate(comment)) {
			throw new ActionNotAllowedException();
		}
		
		return adminAppreciate(user, comment.getId(), comment.getAuthor(), 
				beneficiaries, PyUtils.convertBDFromString(dto.getAppreciation()), dto, 
				PAYMENT_TYPE.APPRECIATE_COMMENT, ipAddress, success, cancel);
	}
	
	@Override
	public ResultSuccessDTO appreciatePosting(User user, Posting posting, 
			AppreciatePostingDTO dto, String ipAddress)
					throws ServiceException {
		ArgCheck.nullCheck(posting, dto, ipAddress);
		ArgCheck.userCheck(user);
		List<CachedUsername> beneficiaries = new ArrayList<CachedUsername>();
		if(posting.getBeneficiary() != null) {
			beneficiaries.add(posting.getBeneficiary());
		}
		
		if(!postingService.canAppreciate(posting)) {
			throw new ActionNotAllowedException();
		}
		
		return appreciate(user, posting.getId(), posting.getAuthor(), 
				beneficiaries, PyUtils.convertBDFromString(dto.getAppreciation()), dto, 
				PAYMENT_TYPE.APPRECIATE_POSTING, ipAddress, success, cancel);
	}
	
	@Override
	public ResultSuccessDTO appreciateComment(User user, Comment comment, 
			AppreciateCommentDTO dto, String ipAddress)
					throws ServiceException {
		ArgCheck.nullCheck(comment, dto, ipAddress);
		ArgCheck.userCheck(user);
		List<CachedUsername> beneficiaries = new ArrayList<CachedUsername>();
		if(comment.getBeneficiary() != null) {
			beneficiaries.add(comment.getBeneficiary());
		}
		
		if(!commentService.canAppreciate(comment)) {
			throw new ActionNotAllowedException();
		}
		
		return appreciate(user, comment.getId(), comment.getAuthor(), 
				beneficiaries, PyUtils.convertBDFromString(dto.getAppreciation()), dto, 
				PAYMENT_TYPE.APPRECIATE_COMMENT, ipAddress, success, cancel);
	}
	
	protected void validUserIds(User... users) throws ServiceException {
		ArgCheck.userCheck(users);
		for(User u : users) {
			if(u.getId() == null || u.getPaymentId() == null || u.getUsername() == null) {
				throw new PaymentTargetException();
			}
		}
	}
	
	protected ResultSuccessDTO adminAppreciate(User source, ObjectId referenceId, 
			CachedUsername cachedAuthor, List<CachedUsername> beneficiaries, 
			BigDecimal amount, DTO dto, PAYMENT_TYPE type, String ipAddress, String success, 
			String cancel) throws ServiceException {
		ArgCheck.nullCheck(referenceId, cachedAuthor, type, ipAddress);
		ArgCheck.userCheck(source);
		ArgCheck.nullCheck(source.getId(), source.getUsername());
		
		List<Receiver> receiverList = new ArrayList<Receiver>();
		
		CachedUsername cachedSource = new CachedUsername(source.getId(), source.getUsername());
		User target = userService.findUser(cachedAuthor.getId());
		try {
			validUserIds(target);
		} catch(PaymentTargetException pte) {
			try {
				eventService.eventAppreciationAttempt(cachedSource, cachedAuthor, amount.doubleValue());
			} catch(Exception e) {
				logger.info("Error with event appreciation attempt for user (" + cachedSource.getUsername() +
						") to user (" + cachedAuthor.getUsername() + ") for amount: " + amount + ".");
			}
			throw pte;
		}
		
		BigDecimal correctAmount = PyUtils.formatBDDown(amount);
		
		Map<ObjectId, String> beneficiaryIds = null;
		int beneficiariesSize = 0;
		BigDecimal beneficiariesAmount = new BigDecimal(Double.toString(0.0d));
		if(beneficiaries != null) {
			beneficiariesSize = beneficiaries.size();
			beneficiaryIds = new HashMap<ObjectId, String>();
			
			BigDecimal beneficiaryPer = PyUtils.formatBDDown(correctAmount.divide(new BigDecimal(beneficiariesSize + 1)));
			
			for(CachedUsername cu : beneficiaries) {
				try {
					User u = userService.findUser(cu.getId());
					validUserIds(u);
					beneficiaryIds.put(u.getId(), u.getPaymentId());
					receiverList.add(constructReceiver(beneficiariesAmount.doubleValue(), false, 
							u.getPaymentId()));
					beneficiariesAmount = beneficiariesAmount.add(beneficiaryPer);
				} catch(PaymentTargetException pte) {
					// log, but do nothing, and do not add to receiver list
					try {
						eventService.eventAppreciationAttempt(cachedSource, cu, amount.doubleValue());
					} catch(Exception e) {
						logger.info("Error with event appreciation attempt for user (" + cachedSource.getUsername() +
								") to beneficiary (" + cu.getUsername() + ") for amount: " + amount + ".");
					}
				} catch(NotFoundException nfe) {
					// log, but do nothing, and do not add to receiver list
					logger.info("User with cached username {" + cu + "} was not found and could not be added as a payment target!", nfe);
				} catch(Exception e) {
					logger.info("Null user was a beneficiary and could nto be added as a payment target!");
				}
			}
		}
		BigDecimal targetAmount = PyUtils.formatBDDown(correctAmount).subtract(beneficiariesAmount);
		
		receiverList.add(constructReceiver(targetAmount.doubleValue(), false, target.getPaymentId()));
		
		PayRequest payRequest = constructMultiPayRequest(receiverList, ipAddress, success, cancel);
		String caU = cachedAuthor.getUsername();
		if(caU != null && caU.length() > 0) {
			payRequest.setMemo(appreciationMemo + cachedAuthor.getUsername());
		} else {
			payRequest.setMemo(appreciationMemoAlt);
		}
		
		String resultUrl = payment(payRequest, type, referenceId, source.getId(), 
				target.getId(), target.getPaymentId(), 
				beneficiaryIds, correctAmount, dto, success, cancel);

		logger.debug("Appreciation by user (" + source.getUsername() + ") with id {" 
				+ source.getId().toHexString() + "}.");
		
		ResultSuccessDTO result = new ResultSuccessDTO();
		result.setResult(resultUrl);
		return result;
	}
	
	protected ResultSuccessDTO appreciate(User source, ObjectId referenceId, 
			CachedUsername cachedAuthor, List<CachedUsername> beneficiaries, 
			BigDecimal amount, DTO dto, PAYMENT_TYPE type, String ipAddress, String success, 
			String cancel) throws ServiceException {
		ArgCheck.nullCheck(referenceId, cachedAuthor, type, ipAddress);
		ArgCheck.userCheck(source);
		ArgCheck.nullCheck(source.getId(), source.getUsername());
		
		List<Receiver> receiverList = new ArrayList<Receiver>();
		
		CachedUsername cachedSource = new CachedUsername(source.getId(), source.getUsername());
		User target = userService.findUser(cachedAuthor.getId());
		try {
			validUserIds(target);
		} catch(PaymentTargetException pte) {
			try {
				eventService.eventAppreciationAttempt(cachedSource, cachedAuthor, amount.doubleValue());
			} catch(Exception e) {
				logger.info("Error with event appreciation attempt for user (" + cachedSource.getUsername() +
						") to user (" + cachedAuthor.getUsername() + ") for amount: " + amount + ".");
			}
			throw pte;
		}
		
		// calculation of fees
		//BigDecimal amountBd = new BigDecimal(Double.toString(amount));
		//amountBd = amountBd.setScale(ServiceValues.PAYMENT_DOUBLE_PRECISION,
		//		BigDecimal.ROUND_FLOOR);
		//double correctAmount = amountBd.doubleValue();
		//double pretax = PyUtils.formatCurrencyDown(amount);
		
		BigDecimal pretax = PyUtils.formatBDDown(amount);
		
		//double pretaxFee = PyUtils.formatCurrencyUp(pretax * ServiceValues.PAYMENT_APPRECIATION_FEE);
		//double tax = PyUtils.formatCurrencyUp(financeService.getTaxFromCost(pretaxFee));
		//double fee = PyUtils.formatCurrencyDown(pretaxFee + tax);
		BigDecimal pretaxFee = getPromotionFee(pretax);
		BigDecimal tax = financeService.getTaxFromCost(pretaxFee);
		
		//BigDecimal feeBd = new BigDecimal(Double.toString(fee));
		//feeBd = feeBd.setScale(2, BigDecimal.ROUND_HALF_UP);
		//fee = feeBd.doubleValue();
		
		BigDecimal fee = PyUtils.formatBDDown(pretaxFee.add(tax));
		
		//double correctAmount = pretax.add(PyUtils.formatBDDown(tax));
		BigDecimal correctAmount = PyUtils.formatBDDown(pretax.add(tax));
		
		receiverList.add(getChargeReceiver(fee.doubleValue()));
		
		Map<ObjectId, String> beneficiaryIds = null;
		int beneficiariesSize = 0;
		BigDecimal beneficiariesAmount = new BigDecimal(Double.toString(0.0d));
		if(beneficiaries != null) {
			beneficiariesSize = beneficiaries.size();
			beneficiaryIds = new HashMap<ObjectId, String>();
			
			// split between beneficiaries and targets (beneficiaries.size + 1)
			BigDecimal beneficiaryPer = PyUtils.formatBDDown(correctAmount.subtract(fee).divide(new BigDecimal(beneficiariesSize + 1)));
			//BigDecimal beneficiaryBd = new BigDecimal(Double.toString(beneficiaryPer));
			//beneficiaryBd = beneficiaryBd.setScale(ServiceValues.PAYMENT_DOUBLE_PRECISION, 
			//		BigDecimal.ROUND_FLOOR);
			//beneficiariesAmount = beneficiaryBd.doubleValue();
			
			for(CachedUsername cu : beneficiaries) {
				try {
					User u = userService.findUser(cu.getId());
					validUserIds(u);
					beneficiaryIds.put(u.getId(), u.getPaymentId());
					receiverList.add(constructReceiver(beneficiariesAmount.doubleValue(), false, 
							u.getPaymentId()));
					beneficiariesAmount = beneficiariesAmount.add(beneficiaryPer);
				} catch(PaymentTargetException pte) {
					// log, but do nothing, and do not add to receiver list
					try {
						eventService.eventAppreciationAttempt(cachedSource, cu, amount.doubleValue());
					} catch(Exception e) {
						logger.info("Error with event appreciation attempt for user (" + cachedSource.getUsername() +
								") to beneficiary (" + cu.getUsername() + ") for amount: " + amount + ".");
					}
				} catch(NotFoundException nfe) {
					// log, but do nothing, and do not add to receiver list
					logger.info("User with cached username {" + cu + "} was not found and could not be added as a payment target!", nfe);
				} catch(Exception e) {
					logger.info("Null user was a beneficiary and could nto be added as a payment target!");
				}
			}
		}
		
		//BigDecimal targetBd = new BigDecimal(Double.toString(correctAmount));
		//targetBd = targetBd.setScale(ServiceValues.PAYMENT_DOUBLE_PRECISION,
		//		BigDecimal.ROUND_FLOOR);
		//double targetAmount = targetBd.doubleValue();
		BigDecimal targetAmount = PyUtils.formatBDDown(correctAmount).subtract(beneficiariesAmount).subtract(fee);
		
		//receiverList.add(constructReceiver(targetAmount.doubleValue(), true, target.getPaymentId()));
		receiverList.add(constructReceiver(targetAmount.doubleValue(), false, target.getPaymentId()));
		
		PayRequest payRequest = constructMultiPayRequest(receiverList, ipAddress, success, cancel);
		String caU = cachedAuthor.getUsername();
		if(caU != null && caU.length() > 0) {
			payRequest.setMemo(appreciationMemo + cachedAuthor.getUsername());
		} else {
			payRequest.setMemo(appreciationMemoAlt);
		}
		
		String resultUrl = payment(payRequest, type, referenceId, source.getId(), 
				target.getId(), target.getPaymentId(), 
				beneficiaryIds, correctAmount, dto, success, cancel);

		logger.debug("Appreciation by user (" + source.getUsername() + ") with id {" 
				+ source.getId().toHexString() + "}.");
		
		ResultSuccessDTO result = new ResultSuccessDTO();
		result.setResult(resultUrl);
		return result;
	}
	
	protected String payment(PayRequest payRequest, PAYMENT_TYPE type, ObjectId referenceId, 
			ObjectId sourceId, ObjectId targetId, 
			String targetPaymentId, Map<ObjectId, String> beneficiaries, BigDecimal amount, 
			DTO dto, String successUrl, String cancelUrl) throws ServiceException {
		ArgCheck.nullCheck(payRequest, type, sourceId, dto, successUrl, cancelUrl);
		
		UriComponentsBuilder uriBuilder = null;
		long longAmount = 0l;
		if(amount != null) {
			longAmount = PyUtils.convertFromCurrency(amount);
		}
		try {
			uriBuilder = UriComponentsBuilder.fromUriString(successUrl);
		} catch(Exception e) {
			throw new BadParameterException();
		}
		
		Payment payment = null;
		try {
			payment = paymentDao.initializePayment(type, referenceId, sourceId,
					targetId, targetPaymentId, beneficiaries, longAmount, dto);

			if(!paymentDao.verifyPayment(payment.getId(), PAYMENT_STATE.INITIAL, type, 
					referenceId, sourceId, targetId, targetPaymentId, 
					beneficiaries, longAmount, dto)) {
				failPayment(payment);
				throw new PaymentException();
			}
		} catch(DaoException de) {
			failPayment(payment);
			throw new PaymentException();
		}
		if(payment == null || payment.getId() == null) {
			failPayment(payment);
			throw new PaymentException();
		}
		ObjectId paymentId = payment.getId();
		
		// tracking id set here when payment id can be found
		payRequest.setTrackingId(paymentId.toHexString());
		try {
			uriBuilder.queryParam(ParamNames.TRACKING_ID, paymentId.toHexString());
			payRequest.setReturnUrl(uriBuilder.build().toUriString());
		} catch(Exception e) {
			failPayment(payment);
			throw new PaymentException();
		}
		
		PayResponse response = null;
		String payKey = null;
		try {
			response = ppAps.pay(payRequest);
		} catch(Exception e) {
			logger.warn("Could not make payment with id {" + paymentId + "}.");
			failPayment(payment);
			throw new ExternalServiceException();
		}
		if(response == null) {
			logger.warn("No response when attempting to make payment with id {" + paymentId + "}.");
			failPayment(payment);
			throw new ExternalServiceException();
		}
		if(!responseEnvelopeSuccessful(response.getResponseEnvelope())) {
			logger.warn("Response was unsuccessful while attempting to make payment with id {" + paymentId + "}.");
			failPayment(payment);
			throw new ExternalServiceException();
		}
		
		try {
			dealWithPaymentExecStatus(response.getPaymentExecStatus(), EXEC_STATUS.CREATED);
		} catch(Exception e) {
			logger.warn("Payment status was not correct when attempting to make payment with id {" + paymentId + "}.");
			failPayment(payment);
			throw new ExternalServiceException();
		}
		
		payKey = response.getPayKey();
		if(payKey == null) {
			logger.warn("PayKey not found while attempting to make payment with id {" + paymentId + "}.");
			failPayment(payment);
			throw new ExternalServiceException();
		}
		
		// set paykey
		try {
			paymentDao.setPayKey(paymentId, PAYMENT_STATE.CREATED, payKey);
			if(!paymentDao.checkPayKey(paymentId, PAYMENT_STATE.CREATED, payKey)) {
				failPayment(payment);
				throw new PaymentException();
			}
		} catch(DaoException de) {
			logger.warn("PayKey was not set for payment with id {" + paymentId + "}.");
			failPayment(payment);
			throw new PaymentException();
		}
		UriComponentsBuilder returnUriBuilder = 
				UriComponentsBuilder.fromUriString(redirectPaymentUrl);
		returnUriBuilder.queryParam(ParamNames.PAYKEY, payKey);
		return returnUriBuilder.build().toUriString();
	}
	
	protected void failPayment(Payment payment) throws ServiceException {
		if(payment == null) {
			logger.warn("Payment was null when failing!");
			throw new PaymentException();
		}
		logger.info("Failing payment {" + payment.getId() + "}.");
		try {
			paymentDao.updatePaymentState(payment.getId(), PAYMENT_STATE.FAILURE);
		} catch(DaoException de) {
			// all hope is lost! must cancel when next connected to the database
		}
	}
	
	protected void cancelPayment(Payment payment) throws ServiceException {
		if(payment == null) {
			throw new PaymentException();
		}
		try {
			paymentDao.updatePaymentState(payment.getId(), PAYMENT_STATE.CANCELLED);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	protected void completePayment(Payment payment) throws ServiceException {
		if(payment == null || payment.getType() == null || payment.getId() == null) {
			throw new PaymentException();
		}
		ObjectId paymentId = payment.getId();

		// check if completed already
		PAYMENT_STATE state = payment.getState();
		if(state == PAYMENT_STATE.COMPLETED) {
			return;
		} else if(state == PAYMENT_STATE.COMPLETION_ERROR) {
			// continue once, mark as completion failure if it 
			// cannot correct this time around
		} else if(state == PAYMENT_STATE.COMPLETION_FAILURE) {
			// not the first time this has happened, LOG THIS this is EXTREMELY bad
			// there is some sort of unrecoverable error, but the user has paid
			// for a service that they have not receive
			// log this
			logger.warn("Could not complete on second try for payment with id {" + paymentId + "}.");
			throw new PaymentException();
		} else if(state == PAYMENT_STATE.APPROVED) {
			// no need to mark it approved again
		} else {
			// continue
			try {
				paymentDao.updatePaymentState(paymentId, PAYMENT_STATE.APPROVED);
			} catch(DaoException de) {
				throw new ServiceException(de);
			}
		}
		
		ServiceException lastException = null;
		try {
			if(payment.getType() == PAYMENT_TYPE.PURCHASE_CURRENCY) {
				PurchaseCurrencyDTO dto = null;
				if(payment.getDto() != null) {
					dto = (PurchaseCurrencyDTO)payment.getDto();
				}
				financeService.purchaseCurrency(paymentId, payment.getSourceId(), dto);
			} else if(payment.getType() == PAYMENT_TYPE.APPRECIATE_POSTING) {
				AppreciatePostingDTO dto = null;
				if(payment.getDto() != null) {
					dto = (AppreciatePostingDTO)payment.getDto();
				}
				
				postingService.appreciatePosting(paymentId,
						userService.findUser(payment.getSourceId()), 
						postingService.getPosting(payment.getReferenceId()),
						getAppreciationAmount(dto.getAppreciation()), 
						getPromotionAmount(dto.getAppreciation()), 
						dto.getTags(), 
						dto.isWarning());
			} else if(payment.getType() == PAYMENT_TYPE.APPRECIATE_COMMENT) {
				AppreciateCommentDTO dto = null;
				if(payment.getDto() != null) {
					dto = (AppreciateCommentDTO)payment.getDto();
				}
	
				commentService.appreciateComment(paymentId,
						userService.findUser(payment.getSourceId()), 
						commentService.getComment(payment.getReferenceId()),
						getAppreciationAmount(dto.getAppreciation()), 
						getPromotionAmount(dto.getAppreciation()), 
						dto.isWarning());
			} else {
				// incomplete code here! a new payment type was added without handling it
				logger.warn("A payment type was not checked while attempting to complete payment with id {" + paymentId + "}.");
			}
		} catch(ServiceException se) {
			// check if it was completed. It is possible the transaction was completed,
			// but an error was thrown elsewhere. The transaction needs to be checked for
			// completion and payment status updated accordingly
			logger.warn("An exception occured while completing payment with id {" + paymentId + "}.", se);
			lastException = se;
		}
		
		PAYMENT_STATE completionState = PAYMENT_STATE.COMPLETED;
		if(transactionCompleted(paymentId)) {
			completionState = PAYMENT_STATE.COMPLETED;
		} else {
			if(state == PAYMENT_STATE.COMPLETION_ERROR) {
				completionState = PAYMENT_STATE.COMPLETION_FAILURE;
			} else {
				completionState = PAYMENT_STATE.COMPLETION_ERROR;
			}
		}
		
		try {
			paymentDao.updatePaymentState(paymentId, completionState);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(lastException != null) {
			throw lastException;
		}
	}
	
	protected boolean transactionCompleted(ObjectId paymentId) throws ServiceException {
		return financeService.completedPaymentTransaction(paymentId);
	}
	
	protected long getAppreciationAmount(String appreciation) {
		BigDecimal zero = new BigDecimal(Double.toString(0.0d));
		try {
			BigDecimal app = new BigDecimal(appreciation);
			if(appreciation == null || app == null || app.compareTo(zero) <= 0) {
				return 0l;
			}
			return PyUtils.convertFromCurrency(app);
		} catch(Exception e) {
			return 0l;
		}
	}
	
	protected long getPromotionAmount(String appreciation) {
		BigDecimal zero = new BigDecimal(Double.toString(0.0d));
		try {
			BigDecimal app = new BigDecimal(appreciation);
			if(appreciation == null || app == null || app.compareTo(zero) <= 0) {
				return 0l;
			}
			BigDecimal promotion = getPromotionFee(app);
			return financeService.getCurrencyFromCost(promotion);
		} catch(Exception e) {
			return 0l;
		}
	}

	public String getSelfPaymentId() {
		return selfPaymentId;
	}
	
	protected boolean responseEnvelopeSuccessful(ResponseEnvelope re) {
		if(re != null && re.getAck() != null
				&& re.getAck().getValue() != null
				&& (re.getAck().getValue().equalsIgnoreCase(AckCode.SUCCESS.toString()) 
					|| re.getAck().getValue().equalsIgnoreCase(
							AckCode.SUCCESSWITHWARNING.toString()))) {
			return true;
		}
		return false;
	}
	
	protected enum EXEC_STATUS {
		CREATED,
		COMPLETED,
		INCOMPLETE,
		ERROR,
		REVERSALERROR,
		PROCESSING,
		PENDING,
		EXPIRED
	}
	
	protected void dealWithPaymentExecStatus(String status, EXEC_STATUS... expected)
			throws ServiceException {
		if(status != null && expected != null) {
			for(EXEC_STATUS expectedStatus : expected) {
				if(status.equalsIgnoreCase(expectedStatus.toString())) {
					return;
				}
			}
		}
		// reverse, revert, rollback, or cancel these payments
		throw new ExternalServiceException();
	}
	
	@Override
	public void markOldPayments() throws ServiceException {
		List<PAYMENT_STATE> states = new ArrayList<PAYMENT_STATE>();
		states.add(PAYMENT_STATE.INITIAL);
		states.add(PAYMENT_STATE.CREATED);
		states.add(PAYMENT_STATE.APPROVED);
		states.add(PAYMENT_STATE.COMPLETION_ERROR);
		
		Date then = new Date((new Date()).getTime() - ServiceValues.PAYMENT_MARK_TIME);
		try {
			paymentDao.markPayments(null, states, then, PAYMENT_MARK.NONE, PAYMENT_MARK.CHECK);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void checkPaymentBatch(List<PAYMENT_TYPE> types, List<PAYMENT_STATE> states, 
			Date olderThanModified) throws ServiceException {
		
		if(types != null && types.isEmpty()) {
			types = null;
		}
		if(states != null && states.isEmpty()) {
			states = null;
		}
		
		ServiceException lastException = null;
		//int exceptionCount = 0;
		
		Pageable pageable = new PageRequest(0, ServiceValues.PAYMENT_BATCH_SIZE);
		Date then = olderThanModified;
		Page<Payment> payments = null;
		try {
			payments = paymentDao.getPayments(types, states, then, PAYMENT_MARK.NONE, pageable);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(payments == null) {
			throw new ServiceException();
		}
		for(Payment payment : payments.getContent()) {
			try {
				checkPaymentStatus(payment);
			} catch(ActionNotAllowedException anae) {
				logger.debug("Payment was checked and incomplete with id {" + payment + "}.", anae);
			} catch(PaymentException pe) {
				logger.info("Failed to handle payment {" + payment + "}.", pe);
			} catch(ServiceException e) {
				logger.info("Internal failure to handle payment {" + payment + "}.", e);
				//exceptionCount++;
				lastException = e;
			}
		}
		if(lastException != null) {
			throw lastException;
		}
		// do not loop through other pages, just one batch per call
	}
	
	@Override
	public void checkRequested() throws ServiceException {
		Exception lastException = null;
		Pageable pageable = new PageRequest(0, ServiceValues.PAYMENT_BATCH_SIZE);
		Date then = null;
		Page<Payment> payments = null;
		try {
			payments = paymentDao.getPayments(null, null, then, PAYMENT_MARK.CHECK, pageable);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(payments == null) {
			throw new ServiceException();
		}
		for(Payment payment : payments.getContent()) {
			try {
				checkPaymentStatus(payment);
			} catch(ActionNotAllowedException anae) {
				logger.debug("Payment was checked and incomplete with id {" + payment + "}.", anae);
			} catch(PaymentException pe) {
				logger.info("Failed to handle payment {" + payment + "}.", pe);
			} catch(ServiceException e) {
				logger.info("Internal failure to handle payment {" + payment + "}.", e);
				//exceptionCount++;
				lastException = e;
			} finally {
				try {
					Date when = PyUtils.getOldDate(ServiceValues.PAYMENT_EXPIRATION_PERIOD);
					if(payment.getLastModified() == null || payment.getLastModified().before(when)) {
						paymentDao.updatePaymentState(payment.getId(), PAYMENT_STATE.FAILURE, null, PAYMENT_MARK.NONE);
						logger.info("Failing payment {" + payment.getId() + "} due to both expiry and failure.");
					} else {
						paymentDao.markPayment(payment.getId(), null, PAYMENT_MARK.NONE);
					}
				} catch(Exception e) {
					lastException = e;
				}
			}
		}
		if(lastException != null) {
			throw new ServiceException(lastException);
		}
		// do not loop through other pages, just one batch per call
	}

	@Override
	public void checkApproved() throws ServiceException {
		Exception lastException = null;
		Pageable pageable = new PageRequest(0, ServiceValues.PAYMENT_BATCH_SIZE);
		Date then = null;
		Page<Payment> payments = null;
		try {
			payments = paymentDao.getPayments(null, null, then, PAYMENT_MARK.APPROVED, pageable);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(payments == null) {
			throw new ServiceException();
		}
		for(Payment payment : payments.getContent()) {
			try {
				checkApprovedPaymentStatus(payment);
			} catch(ActionNotAllowedException anae) {
				logger.debug("Payment was checked and incomplete with id {" + payment + "}.", anae);
			} catch(PaymentException pe) {
				logger.info("Failed to handle payment {" + payment + "}.", pe);
			} catch(ServiceException e) {
				logger.info("Internal failure to handle payment {" + payment + "}.", e);
				//exceptionCount++;
				lastException = e;
			} finally {
				try {
					paymentDao.markPayment(payment.getId(), null, PAYMENT_MARK.NONE);
				} catch(Exception e) {
					lastException = e;
				}
			}
		}
		if(lastException != null) {
			throw new ServiceException(lastException);
		}
		// do not loop through other pages, just one batch per call
	}
	
	@Override
	public void removeFinishedPayments() throws ServiceException {
		List<PAYMENT_STATE> states = new ArrayList<PAYMENT_STATE>();
		// do not remove completed payments, these should be left in the system as a record
		states.add(PAYMENT_STATE.CANCELLED);
		states.add(PAYMENT_STATE.FAILURE);
		Date then = PyUtils.getOldDate(ServiceValues.PAYMENT_EXPIRATION_REMOVAL_PERIOD);
		try {
			paymentDao.remove(states, then);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
}
