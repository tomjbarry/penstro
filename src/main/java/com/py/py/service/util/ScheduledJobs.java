package com.py.py.service.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.py.py.constants.ServiceValues;
import com.py.py.domain.enumeration.AGGREGATION_TYPE;
import com.py.py.domain.enumeration.DEAL_STATE;
import com.py.py.enumeration.ADMIN_STATE;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.service.AdminService;
import com.py.py.service.CommentService;
import com.py.py.service.EmailService;
import com.py.py.service.EscrowService;
import com.py.py.service.FinanceService;
import com.py.py.service.FlagService;
import com.py.py.service.MessageService;
import com.py.py.service.PaymentService;
import com.py.py.service.PostingService;
import com.py.py.service.TagService;
import com.py.py.service.UserService;
import com.py.py.service.base.BaseAggregator;
import com.py.py.service.exception.FeatureDisabledException;
import com.py.py.util.PyLogger;

public class ScheduledJobs {

	protected static final PyLogger logger = PyLogger.getLogger(ScheduledJobs.class);
	
	@Autowired
	protected UserService userService;
	
	@Autowired
	protected EscrowService escrowService;
	
	@Autowired
	protected TagService tagService;
	
	@Autowired
	protected PostingService postingService;
	
	@Autowired
	protected CommentService commentService;
	
	@Autowired
	protected EmailService emailService;
	
	@Autowired
	protected PaymentService paymentService;
	
	@Autowired
	protected FinanceService financeService;
	
	@Autowired
	protected AdminService adminService;
	
	@Autowired
	protected MessageService messageService;
	
	@Autowired
	protected FlagService flagService;
	
	private Date lastSortedTags;
	//private Date lastCheckedAllPayments;
	
	public void cleanupAggregations() {
		try {
			// just use the tag service
			((BaseAggregator)tagService).removeExpired(AGGREGATION_TYPE.USER);
			((BaseAggregator)tagService).removeExpired(AGGREGATION_TYPE.TAG);
			((BaseAggregator)tagService).removeExpired(AGGREGATION_TYPE.POSTING);
			((BaseAggregator)tagService).removeExpired(AGGREGATION_TYPE.COMMENT);
		} catch(Exception e) {
			logger.error("Aggregation exception while cleaning up aggregations!");
		}
	}
	
	public void updateUsersTotals() {
		try {
			Date before = new Date();
			userService.aggregateTotals();
			logger.info("Aggregated users totals in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.USER.toString(), e);
		}
	}
	
	public void aggregateUsersHour() {
		try {
			Date before = new Date();
			userService.aggregateUsers(TIME_OPTION.HOUR);
			logger.info("Aggregated users for hour in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.USER.toString(), e);
		}
	}
	
	public void aggregateUsersDay() {
		try {
			Date before = new Date();
			userService.aggregateUsers(TIME_OPTION.DAY);
			logger.info("Aggregated users for day in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.USER.toString(), e);
		}
	}
	
	public void aggregateUsersMonth() {
		try {
			Date before = new Date();
			userService.aggregateUsers(TIME_OPTION.MONTH);
			logger.info("Aggregated users for month in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.USER.toString(), e);
		}
	}
	
	public void aggregateUsersYear() {
		try {
			Date before = new Date();
			userService.aggregateUsers(TIME_OPTION.YEAR);
			logger.info("Aggregated users for year in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.USER.toString(), e);
		}
	}
	
	public void updateTagsTotals() {
		try {
			Date before = new Date();
			tagService.aggregateTotals();
			logger.info("Aggregated tags totals in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.TAG.toString(), e);
		}
	}
	
	public void aggregateTagsHour() {
		try {
			Date before = new Date();
			tagService.aggregateTags(TIME_OPTION.HOUR);
			logger.info("Aggregated tags for hour in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.TAG.toString(), e);
		}
	}
	
	public void aggregateTagsDay() {
		try {
			Date before = new Date();
			tagService.aggregateTags(TIME_OPTION.DAY);
			logger.info("Aggregated tags for day in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.TAG.toString(), e);
		}
	}
	
	public void aggregateTagsMonth() {
		try {
			Date before = new Date();
			tagService.aggregateTags(TIME_OPTION.MONTH);
			logger.info("Aggregated tags for month in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.TAG.toString(), e);
		}
	}
	
	public void aggregateTagsYear() {
		try {
			Date before = new Date();
			tagService.aggregateTags(TIME_OPTION.YEAR);
			logger.info("Aggregated tags for year in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.TAG.toString(), e);
		}
	}
	
	public void updatePostingsTotals() {
		try {
			Date before = new Date();
			postingService.aggregateTotals();
			logger.info("Aggregated postings totals in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.POSTING.toString(), e);
		}
	}
	
	public void aggregatePostingsHour() {
		try {
			Date before = new Date();
			postingService.aggregatePostings(TIME_OPTION.HOUR);
			logger.info("Aggregated postings for hour in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.POSTING.toString(), e);
		}
	}
	
	public void aggregatePostingsDay() {
		try {
			Date before = new Date();
			postingService.aggregatePostings(TIME_OPTION.DAY);
			logger.info("Aggregated postings for day in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.POSTING.toString(), e);
		}
	}
	
	public void aggregatePostingsMonth() {
		try {
			Date before = new Date();
			postingService.aggregatePostings(TIME_OPTION.MONTH);
			logger.info("Aggregated postings for month in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.POSTING.toString(), e);
		}
	}
	
	public void aggregatePostingsYear() {
		try {
			Date before = new Date();
			postingService.aggregatePostings(TIME_OPTION.YEAR);
			logger.info("Aggregated postings for year in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.POSTING.toString(), e);
		}
	}
	
	public void updateCommentsTotals() {
		try {
			Date before = new Date();
			commentService.aggregateTotals();
			logger.info("Aggregated comments totals in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.COMMENT.toString(), e);
		}
	}
	
	public void aggregateCommentsHour() {
		try {
			Date before = new Date();
			commentService.aggregateComments(TIME_OPTION.HOUR);
			logger.info("Aggregated comments for hour in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.COMMENT.toString(), e);
		}
	}
	
	public void aggregateCommentsDay() {
		try {
			Date before = new Date();
			commentService.aggregateComments(TIME_OPTION.DAY);
			logger.info("Aggregated comments for day in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.COMMENT.toString(), e);
		}
	}
	
	public void aggregateCommentsMonth() {
		try {
			Date before = new Date();
			commentService.aggregateComments(TIME_OPTION.MONTH);
			logger.info("Aggregated comments for month in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.COMMENT.toString(), e);
		}
	}
	
	public void aggregateCommentsYear() {
		try {
			Date before = new Date();
			commentService.aggregateComments(TIME_OPTION.YEAR);
			logger.info("Aggregated comments for year in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Aggregation exception for type: " + AGGREGATION_TYPE.COMMENT.toString(), e);
		}
	}
	
	public void runEmailService() {
		try {
			Date before = new Date();
			emailService.sendEmails();
			logger.info("Sent emails in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception sending emails!", e);
		}
	}
	
	public void cleanupEmailServiceCompleted() {
		try {
			Date before = new Date();
			emailService.cleanupCompleted();
			logger.info("Removed completed emails in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception removing completed emails!", e);
		}
	}
	
	public void cleanupEmailServiceErrors() {
		try {
			Date before = new Date();
			emailService.cleanupCompleted();
			logger.info("Removed email task errors in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception removing email task errors!", e);
		}
	}
	
	public void updatePostingTags() {
		try {
			Date before = new Date();
			if(lastSortedTags == null) {
				lastSortedTags = new Date((new Date()).getTime() - ServiceValues.SORT_POSTING_TAGS_DEFAULT_TIME_SINCE);
			}
			postingService.sortTags(lastSortedTags);
			logger.info("Updated all posting tags from after time " + lastSortedTags + " in : " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
			lastSortedTags = before;
		} catch(Exception e) {
			logger.error("Exception updating posting tags", e);
		}
	}
	
	public void checkCompletedPayments() {
		Date before = new Date();

		try {
			paymentService.checkApproved();
			logger.info("Checked approved payments in: " + ((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception checking approved payments.", e);
		}

		before = new Date();
		try {
			paymentService.checkRequested();
			logger.info("Checked requested payments in: " + ((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception checking requested payments.", e);
		}

		before = new Date();
		try {
			paymentService.markOldPayments();
			logger.info("Marked payments in: " + ((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception marking payments.", e);
		}
		/*
		before = new Date();
		// check this to prevent from checking ALL payments on startup, otherwise we are in for a LONG startup time
		if(lastCheckedAllPayments != null && new Date(before.getTime() - ServiceValues.PAYMENT_CHECK_ALL_TIME).after(lastCheckedAllPayments)) {
			lastCheckedAllPayments = before;
			List<PAYMENT_STATE> states = new ArrayList<PAYMENT_STATE>();
			states.add(PAYMENT_STATE.CREATED);
			states.add(PAYMENT_STATE.APPROVED);
			states.add(PAYMENT_STATE.COMPLETION_ERROR);
			// dont check completion failure
			try {
				paymentService.checkPaymentBatch(null, states, null);
				logger.info("Checked payments of states: " + states.toString() + " in: " + 
						((new Date()).getTime() - before.getTime()) + " ms.");
			} catch(Exception e) {
				logger.error("Exception checking payments of states: " + states.toString(), e);
			}
			before = new Date();
			states = new ArrayList<PAYMENT_STATE>();
			states.add(PAYMENT_STATE.UNDEFINED);
			states.add(PAYMENT_STATE.INITIAL);
			// dont check completion failure
			try {
				Date then = new Date((new Date()).getTime() - ServiceValues.PAYMENT_PROCESSING_TIME);
				paymentService.checkPaymentBatch(null, states, then);
				logger.info("Checked payments of states: " + states.toString() + " in: " + 
						((new Date()).getTime() - before.getTime()) + " ms.");
			} catch(Exception e) {
				logger.error("Exception checking payments of states: " + states.toString(), e);
			}
		}
		if(lastCheckedAllPayments == null) {
			lastCheckedAllPayments = new Date((new Date()).getTime() - ServiceValues.PAYMENT_CHECK_ALL_TIME);
		}*/
	}
	
	public void removePayments() {
		Date before = new Date();
		try {
			paymentService.removeFinishedPayments();
			logger.info("Removed finished payments in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception removing finished payments.", e);
		}
	}
	
	public void checkDeals() {
		Date before = new Date();
		List<DEAL_STATE> states = new ArrayList<DEAL_STATE>();
		states.add(DEAL_STATE.UNDEFINED);
		states.add(DEAL_STATE.INITIAL);
		states.add(DEAL_STATE.COMMITTED);
		states.add(DEAL_STATE.PENDING);
		states.add(DEAL_STATE.PENDING_FAILURE);
		try {
			financeService.checkBatchDeals(states);
			logger.info("Checked deals of states: " + states.toString() + " in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception checking deals of states: " + states.toString(), e);
		}
	}
	
	public void removeDeals() {
		Date before = new Date();
		try {
			financeService.removeFinishedDeals();
			logger.info("Removed finished deals in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception removing finished deals.", e);
		}
	}
	
	public void checkAdminActions() {
		Date before = new Date();
		List<ADMIN_STATE> states = new ArrayList<ADMIN_STATE>();
		states.add(ADMIN_STATE.INITIAL);
		states.add(ADMIN_STATE.PENDING_FAILURE);
		try {
			adminService.checkBatchActions(states);
			logger.info("Checked admin actions of states: " + states.toString() + " in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception checking admin actions of states: " 
						+ states.toString(), e);
		}
	}
	
	public void removeAdminActions() {
		Date before = new Date();
		try {
			adminService.removeFailedActions();
			logger.info("Removed failed admin actions in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception removing failed admin actions.", e);
		}
	}
	
	public void removeInvalidEscrows() {
		Date before = new Date();
		try {
			escrowService.cleanupInvalid();
			logger.info("Removed invalid escrows in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(FeatureDisabledException fde) {
			logger.info("Escrow feature disabled. Could not remove invalid escrows.");
		} catch(Exception e) {
			logger.error("Exception removing invalid escrows.", e);
		}
	}
	
	public void refundExpiredEscrowOffers() {
		Date before = new Date();
		try {
			escrowService.refundExpiredOffers();
			logger.info("Refunded expired escrow offers in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(FeatureDisabledException fde) {
			logger.info("Escrow feature disabled. Could not refund expired escrow offers.");
		} catch(Exception e) {
			logger.error("Exception refunding expired escrow offers.", e);
		}
	}
	
	public void removeExpiredCorrespondences() {
		Date before = new Date();
		try {
			messageService.removeExpiredCorrespondences();
			logger.info("Removed expired correspondences in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception removing expired correspondences.", e);
		}
	}
	
	public void renameUsers() {
		Date before = new Date();
		try {
			adminService.renameUsers();
			logger.info("Renamed users in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception renaming users.", e);
		}
	}
	
	public void deleteUsers() {
		Date before = new Date();
		try {
			adminService.deleteUsers();
			logger.info("Deleted users in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception deleting users.", e);
		}
	}
	
	public void completeRenameUsers() {
		Date before = new Date();
		try {
			adminService.completeRenameUsers();
			logger.info("Renamed users in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception renaming users.", e);
		}
	}
	
	public void completeDeleteUsers() {
		Date before = new Date();
		try {
			adminService.completeDeleteUsers();
			logger.info("Deleted users in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception deleting users.", e);
		}
	}
	
	public void markPostingsArchived() {
		Date before = new Date();
		try {
			postingService.markArchived();
			logger.info("Posts marked for archival in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception marking posts as archived.", e);
		}
	}
	
	public void markCommentsArchived() {
		Date before = new Date();
		try {
			commentService.markArchived();
			logger.info("Comments marked for archival in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception marking comments as archived.", e);
		}
	}
	
	public void decrementFlags() {
		Date before = new Date();
		try {
			flagService.decrementAll();
			logger.info("Flag data decremented in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception decrementing flag data.", e);
		}
	}
	
	public void removeFlags() {
		Date before = new Date();
		try {
			flagService.removeOld();
			logger.info("Flag data removed in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception removing flag data.", e);
		}
	}
	
	public void checkInvalidEmails() {
		Date before = new Date();
		try {
			emailService.checkEmailBounces();
			logger.info("Invalid Email from Bounces checked in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception checking email bounces.", e);
		}
		
		before = new Date();
		try {
			emailService.checkEmailComplaints();;
			logger.info("Invalid Email from Complaints checked in: " + 
					((new Date()).getTime() - before.getTime()) + " ms.");
		} catch(Exception e) {
			logger.error("Exception checking email complaints.", e);
		}
	}
}
