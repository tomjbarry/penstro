package com.py.py.service;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.domain.AdminAction;
import com.py.py.domain.Comment;
import com.py.py.domain.Posting;
import com.py.py.domain.User;
import com.py.py.dto.in.admin.ChangeBalanceDTO;
import com.py.py.dto.in.admin.ChangeEmailAdminDTO;
import com.py.py.dto.in.admin.ChangePendingActionsDTO;
import com.py.py.dto.in.admin.ChangeRestrictedDTO;
import com.py.py.dto.in.admin.ChangeRolesDTO;
import com.py.py.dto.in.admin.ChangeTallyDTO;
import com.py.py.dto.in.admin.ChangeUsernameDTO;
import com.py.py.dto.in.admin.LockUserDTO;
import com.py.py.dto.in.admin.SetPasswordDTO;
import com.py.py.dto.out.CommentDTO;
import com.py.py.dto.out.PostingDTO;
import com.py.py.dto.out.RoleSetDTO;
import com.py.py.dto.out.admin.AdminActionDTO;
import com.py.py.dto.out.admin.RestrictedDTO;
import com.py.py.enumeration.ADMIN_STATE;
import com.py.py.enumeration.ADMIN_TYPE;
import com.py.py.enumeration.FLAG_TYPE;
import com.py.py.enumeration.RESTRICTED_TYPE;
import com.py.py.service.exception.ServiceException;

public interface AdminService {

	void unlockUser(User admin, ObjectId targetId) throws ServiceException;

	void lockUser(User admin, ObjectId targetId, LockUserDTO dto) throws ServiceException;

	void setRoles(User admin, ObjectId targetId, ChangeRolesDTO dto) throws ServiceException;

	void setPostingWarning(User admin, ObjectId postingId, boolean warning) throws ServiceException;

	void setCommentWarning(User admin, ObjectId commentId, boolean warning) throws ServiceException;

	void setPostingFlag(User admin, ObjectId postingId, boolean flag) throws ServiceException;

	void setCommentFlag(User admin, ObjectId commentId, boolean flag) throws ServiceException;

	void addBalance(User admin, ObjectId targetId, ChangeBalanceDTO dto) throws ServiceException;

	void removeBalance(User admin, ObjectId targetId, ChangeBalanceDTO dto) throws ServiceException;

	void clearLoginAttempts(User admin, ObjectId targetId) throws ServiceException;

	RoleSetDTO getUserRoleSetDTO(User user) throws ServiceException;

	Posting getPosting(ObjectId postingId) throws ServiceException;

	Comment getComment(ObjectId commentId) throws ServiceException;

	PostingDTO getPostingDTO(Posting posting) throws ServiceException;

	CommentDTO getCommentDTO(Comment comment) throws ServiceException;

	void changePostingTally(User admin, ObjectId referenceId, ChangeTallyDTO dto)
			throws ServiceException;

	void changeCommentTally(User admin, ObjectId referenceId, ChangeTallyDTO dto)
			throws ServiceException;

	void changeEmail(User admin, ObjectId userId, ChangeEmailAdminDTO dto)
			throws ServiceException;

	void addRestricted(User admin, ChangeRestrictedDTO dto) throws ServiceException;

	void removeRestricted(User admin, String word, RESTRICTED_TYPE type)
			throws ServiceException;

	Page<RestrictedDTO> getRestrictedDTOs(RESTRICTED_TYPE type,
			Pageable pageable) throws ServiceException;

	void checkBatchActions(List<ADMIN_STATE> states) throws ServiceException;

	void renameUser(User user, ChangeUsernameDTO dto) throws ServiceException;

	void deleteUser(User user) throws ServiceException;

	void deleteUsers() throws ServiceException;

	void lockTag(User admin, String name, String language) throws ServiceException;

	void unlockTag(User admin, String name, String language) throws ServiceException;

	void renameUsers() throws ServiceException;

	void renameUser(User user) throws ServiceException;

	Page<AdminActionDTO> getAdminDTOs(ObjectId adminId, ADMIN_STATE state,
			ADMIN_TYPE type, String target, Pageable pageable, int direction)
			throws ServiceException;

	void setPostingPaid(User admin, ObjectId postingId, boolean paid)
			throws ServiceException;

	void setCommentPaid(User admin, ObjectId commentId, boolean paid)
			throws ServiceException;

	void continueRename(ObjectId userId, String oldUsername, String newUsername)
			throws ServiceException;

	void completeRename(AdminAction action) throws ServiceException;

	void completeDelete(AdminAction action) throws ServiceException;

	void completeDeleteUsers() throws ServiceException;

	void completeRenameUsers() throws ServiceException;

	void removeFailedActions() throws ServiceException;

	void removeFlagData(User admin, ObjectId id, FLAG_TYPE type)
			throws ServiceException;

	void setPostingRemove(User admin, Posting posting, boolean remove)
			throws ServiceException;

	void setCommentRemove(User admin, Comment comment, boolean remove)
			throws ServiceException;

	void setPendingActions(User admin, ObjectId targetId,
			ChangePendingActionsDTO dto) throws ServiceException;

	void changePassword(User admin, User target, SetPasswordDTO dto) throws ServiceException;

}
