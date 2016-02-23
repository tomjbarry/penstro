package com.py.py.dao.exception;

import com.mongodb.MongoCursorNotFoundException;
import com.mongodb.MongoSocketException;
import com.py.py.util.PyLogger;

public class DaoException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6487676643639954903L;

	protected static final PyLogger logger = PyLogger.getLogger(DaoException.class);

	public DaoException() {
        super();
    }

    public DaoException(String message) {
        super(message);
    }

    public DaoException(String message, Throwable cause) {
        super(message, cause);
        inspectCause(cause);
    }

    public DaoException(Throwable cause) {
        super(cause);
        inspectCause(cause);
    }

    protected DaoException(String message, Throwable cause,
                        boolean enableSuppression,
                        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        inspectCause(cause);
    }
    
    private static void inspectCause(Throwable cause) {
    	if(cause instanceof MongoSocketException) {
    		logger.warn("Network error connecting to the database!", cause);
    	} else if(cause instanceof MongoCursorNotFoundException) {
    		logger.warn("Cursor error on query!", cause);
    	}
    }

}
