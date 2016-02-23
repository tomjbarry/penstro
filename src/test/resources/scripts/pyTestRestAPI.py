from General import General
from Authentication import Authentication
from Role import Role
from User import User
from Finance import Finance
from Notification import Notification
from Message import Message
from Backing import Backing
from Posting import Posting
from Comment import Comment
from Follow import Follow
from Settings import Settings
from Admin import Admin
from Delete import Delete
from PyConstants import CacheTimes
import time

from PyRequest import PyRequest
from random import randrange

def runtests():
    r = str(randrange(1,999999))
    #r = str(123)
    username = "Testing" + r
    email = "t2fhvhd" + r + "@penstro.com"
    target = "Target" + r
    targetEmail = "t2fhvh" + r + "@penstro.com"
    secondary = "Sec" + r
    secondaryEmail = "t2fhvhc" + r + "@penstro.com"
    secondaryTarget = "sT" + r
    secondaryTargetEmail = "t2fhvcc" + r + "@penstro.com"
    lockedTarget1 = "l1t" + r
    lockedTarget1Email = "lt1c" + r + "@penstro.com"
    lockedTarget2 = "l2t" + r
    lockedTarget2Email = "lt2c" + r + "@penstro.com"
    paymentId1="sarin33133@yahoo.com"
    paymentId2="tjbarry@asu.edu"
    paymentId3="pyTest@test.com"
    
    notifications = {'backing':{},
                     'posting':{},
                     'comment':{}}
    
    print("Running all tests")
    print("Username: " + username)
    print("Target: " + target)
    targetToken, token = Authentication(None, username, email, None, target, targetEmail).runTests()
    secondaryTargetToken, secondaryToken = Authentication(None, secondary, secondaryEmail, 
                                                          None, secondaryTarget, 
                                                          secondaryTargetEmail).runTests()
    lockedTargetToken2, lockedTargetToken1 = Authentication(None, lockedTarget1, lockedTarget1Email, 
                                                          None, lockedTarget2, 
                                                          lockedTarget2Email).runTests()
    
    Role(token, username, email, targetToken, target, targetEmail).runTests(secondaryToken, lockedTargetToken1, lockedTargetToken2, paymentId1, paymentId2, paymentId3)
    
    General(token, username, email, targetToken, target, targetEmail).runTests()
    User(token, username, email, targetToken, target, targetEmail).runTests()
    
    Finance(token, username, email, targetToken, target, targetEmail).runTests()
    Notification(token, username, email, targetToken, target, targetEmail).runTests()
    Message(token, username, email, targetToken, target, targetEmail).runTests()
    
    #backing = Backing(token, username, email, targetToken, target, targetEmail)
    #backing.runTests(notifications)
    posting = Posting(token, username, email, targetToken, target, targetEmail)
    createdPostings = posting.runTests(notifications, secondaryToken, secondary, lockedTargetToken1)
    comment = Comment(token, username, email, targetToken, target, targetEmail)
    comment.runTests(notifications, secondaryToken, secondary, createdPostings, lockedTargetToken2)
    Follow(token, username, email, targetToken, target, targetEmail).runTests(secondaryToken, secondary)
    Settings(token, username, email, targetToken, target, targetEmail).runTests()
    
    time.sleep(CacheTimes.PAGED)
    
    print('Testing notifications')
    #backing.testNotifications()
    posting.testNotifications()
    comment.testNotifications()
    
    print('Testing posting and comment pageables')
    #it now takes so long to get to the pageables, due to individual tests requiring wait time for caching, this test is no longer valid
    #posting.testPostingsPaged()
    #comment.testCommentsPaged()
    Admin(token, username, email, targetToken, target, targetEmail).runTests(secondaryToken, secondary, secondaryTargetToken, secondaryTarget)
    
    #Delete(token, username, email, targetToken, target, targetEmail).runTests(secondaryToken, secondaryTargetToken)
    
    print("Finished running all tests")
    if hasattr(PyRequest, "ERROR_COUNT"):
        if PyRequest.ERROR_COUNT > 0:
            print("Error count: " + str(PyRequest.ERROR_COUNT))
        else: 
            print("No errors!")
    else:
        print("No errors!")

if __name__ == '__main__':
    runtests()
