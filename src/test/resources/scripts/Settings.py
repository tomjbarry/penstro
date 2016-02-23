from PyConstants import Paths
from PyConstants import Codes
from PyBaseTest import BaseTest
from PyRequest import PyRequest
from random import randrange

class Settings(BaseTest):
    
    def runTests(self):
        
        self.hiddenFeedEvents = []
        self.hiddenNotificationEvents = []
        print("Running settings tests")
        self.settings = {'options':{'ALLOW_PROFILE_COMMENTS':True,
                                    'ALLOW_WARNING_CONTENT':False},
                         'hiddenFeedEvents':[],
                         'hiddenNotificationEvents':[],
                         'filters':{}, 
                         'language':'en',
                         'interfaceLanguage':'en'}
        self.testSettings()
        self.testChangeNotifications()
        self.testSettings()
        self.testLanguages()
        self.testSettings()
        
    def testSettings(self):
        expected = self.unauthed.getDTOResponse({'options':self.unauthed.insertExists(),
                                                 'hiddenFeedEvents':self.hiddenFeedEvents,
                                                 'hiddenNotificationEvents':self.hiddenNotificationEvents,
                                                 'filters':{},
                                                 'language':self.settings['language'],
                                                 'interfaceLanguage':self.settings['interfaceLanguage']})
        self.unauthed.expectResponse(Paths.SETTINGS, PyRequest.GET, None, self.expectedDenied)
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.GET, None, expected)
        
    def testChangeNotifications(self):
        self.testSettings()
        body = {'options':{'ALLOW_PROFILE_COMMENTS':None}}
        #self.unauthed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedDenied)
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedInvalid)
        
        self.testSettings()
        body = {'options':{'NOT_REAL':True}}
        #self.unauthed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedDenied)
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedInvalid)
        
        self.testSettings()
        body = {'options':{'ALLOW_PROFILE_COMMENTS':False, 'ALLOW_WARNING_CONTENT':True}}
        self.settings['options']['ALLOW_PROFILE_COMMENTS'] = False
        self.settings['options']['ALLOW_WARNING_CONTENT'] = True
        self.unauthed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedDenied)
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedSuccess)
        
        self.testSettings()
        body = {'hiddenFeedEvents':['FOLLOW_GIBBERISH']}
        #self.unauthed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedDenied)
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedInvalid)
        
        self.testSettings()
        body = {'hiddenFeedEvents':['FOLLOW_REMOVE']}
        self.settings['hiddenFeedEvents'] = ['FOLLOW_REMOVE']
        self.unauthed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedDenied)
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedSuccess)
        
        self.testSettings()
        body = {'hiddenFeedEvents':[]}
        self.settings['hiddenFeedEvents'] = []
        self.unauthed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedDenied)
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedSuccess)
        
        body = {'hiddenNotificationEvents':['FOLLOW_GIBBERISH']}
        #self.unauthed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedDenied)
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedInvalid)
        
        self.testSettings()
        body = {'hiddenNotificationEvents':['FOLLOW_REMOVE']}
        self.settings['hiddenNotificationEvents'] = ['FOLLOW_REMOVE']
        self.unauthed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedDenied)
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedSuccess)
        
        self.testSettings()
        body = {'hiddenNotificationEvents':[]}
        self.settings['hiddenNotificationEvents'] = []
        self.unauthed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedDenied)
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedSuccess)
        
        self.testSettings()
        body = {'filters':[{'time':'ALLTIME','sort':'VALUE','tags':['5_3d8cinvalidnotvalidTEST','tom']}]}
        self.settings['filters'] = {0:{'time':'ALLTIME','sort':'VALUE','tags':['5_3d8cinvalidnotvalidTEST','tom']}}
        #self.unauthed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedDenied)
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedInvalid)
        
        self.testSettings()
        body = {'filters':[{'time':'ALLTIME','sort':'VALUE','tags':['TEST','tom']}]}
        self.settings['filters'] = {0:{'time':'ALLTIME','sort':'VALUE','tags':['TEST','tom']}}
        self.unauthed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedDenied)
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedSuccess)
        
        self.testSettings()
        body = {'filters':[{'time':'HOUR','sort':'PROMOTION','tags':[]}]}
        self.settings['filters'] = {0:{'time':'HOUR','sort':'PROMOTION','tags':[]}}
        self.unauthed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedDenied)
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedSuccess)
        
        self.testSettings()
        body = {'hiddenNotificationEvents':[],
                'options':{'ALLOW_PROFILE_COMMENTS':True, 'ALLOW_WARNING_CONTENT':False},
                'hiddenFeedEvents':['FOLLOW_REMOVE'],
                'filters':[{'time':'ALLTIME','sort':'VALUE','tags':['TEST','tom']}]}
        self.settings['hiddenNotificationEvents'] = []
        self.settings['hiddenFeedEvents'] = ['FOLLOW_REMOVE']
        self.settings['options']['ALLOW_PROFILE_COMMENTS'] = True
        self.settings['options']['ALLOW_WARNING_CONTENT'] = False
        self.settings['filters'] = {0:{'time':'ALLTIME','sort':'VALUE','tags':['TEST','tom']}}
        self.unauthed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedDenied)
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedSuccess)
        
        self.testSettings()
        
    def testLanguages(self):
        lang = self.settings['language']
        iLang = self.settings['interfaceLanguage']
        
        body = {'language':'notReal',
                'interfaceLanguage':'en'}
        
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedInvalid)
        self.testSettings()
        
        body = {'language':'fr',
                'interfaceLanguage':'en'}
        
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedSuccess)
        self.settings['language'] = 'fr'
        self.settings['interfaceLanguage'] = 'en'
        self.testSettings()
        
        body = {'interfaceLanguage':'es'}
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedSuccess)
        self.settings['interfaceLanguage'] = 'es'
        self.testSettings()
        
        body = {'language':'en'}
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedSuccess)
        self.settings['language'] = 'en'
        self.testSettings()
        
        body = {'language':'en',
                'interfaceLanguage':'en'}
        self.authed.expectResponse(Paths.SETTINGS, PyRequest.POST, body, self.expectedSuccess)
        self.settings['language'] = 'en'
        self.settings['interfaceLanguage'] = 'en'
        self.testSettings()
        
