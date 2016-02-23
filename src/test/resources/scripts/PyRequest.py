import json
import httplib2 as http
import urllib

class PyRequest(object):
    protocol = 'https'
    locationAdmin = protocol + '://localhost:8010'
    #locationNormal = protocol + '://localhost:8080/api'
    locationNormal = protocol + '://localhost:8010'
    #locationAdmin = protocol + '://api.penstro.com'
    #locationNormal = protocol + '://api.penstro.com'
    tokenHeader = 'Authentication-Token'
    defaultHeaders = {
               'Accept': 'application/json',
               'Content-Type': 'application/json; charset=UTF-8',
               #'User-Agent': 'Mozilla/5.0 (Windows NT 6.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/44.0.2403.130 Safari/537.36'
               'User-Agent': 'pyweb'
               }
    
    GET = 'GET'
    PUT = 'PUT'
    POST = 'POST'
    DELETE = 'DELETE'
    
    CODE = 'code'
    DTO = 'dto'
    PAGE = 'page'
    
    EXISTS = '--$exists$--'
    NOT_EXISTS = '--$not-exists$--'
    BETWEEN = '--$between$--'
    
    def insertExists(self):
        return self.EXISTS
    
    def insertNotExists(self):
        return self.NOT_EXISTS
    
    def insertBetween(self, min=0, max=1):
        return (min, max)
    
    def getCustomResponse(self, code=200, dto=EXISTS, page=NOT_EXISTS):
        return {
                self.CODE:code, 
                self.PAGE:page, 
                self.DTO:dto
                }
    
    def getOnlyCodeResponse(self, code=200):
        return {
                self.CODE:code, 
                self.PAGE:self.insertNotExists(), 
                self.DTO:self.insertNotExists()
                }
    
    def getPageResponse(self, page=EXISTS):
        return {
                self.CODE:200, 
                self.PAGE:page, 
                self.DTO:self.insertNotExists()
                }
    
    def getDTOResponse(self, dto=EXISTS):
        return {
                self.CODE:200, 
                self.PAGE:self.insertNotExists(), 
                self.DTO:dto
                }
    
        
    def __init__(self, token=None, isAdmin=False):
        self.h = http.Http(disable_ssl_certificate_validation=True)
        self.token = str(token)
        self.headers = self.defaultHeaders.copy()
        if token != None:
            self.headers[self.tokenHeader] = self.token
        if not hasattr(PyRequest, 'ERROR_COUNT'):
            PyRequest.ERROR_COUNT = 0
        self.location = self.locationNormal
        if isAdmin:
            self.location = self.locationAdmin
    
    def request(self, path, method, body, pathVariables=None, params=None):
        uri = self.location + path
        if pathVariables != None:
            if isinstance(pathVariables, basestring):
                uri = uri.format(pathVariables)
            else:
                uri = uri.format(*pathVariables)
        if params != None:
            if isinstance(params, (list, tuple)):
                paramString = '?'
                for p in params:
                    pv = p.split("=")
                    if len(pv) > 1:
                        paramString = paramString + urllib.urlencode({pv[0]:pv[1]})
                    else:
                        paramString = paramString + str(pv[0])
                    paramString = paramString + "&"
                paramString = paramString[0:len(paramString) - 1]
            else:
                paramString = '?' + params
            uri = uri + paramString
        bodyString = None
        if body != None:
            bodyString = json.dumps(body)
        response, content = self.h.request(uri, method, bodyString, self.headers)
        data = {}
        if content != None and content != "":
            data = json.loads(content)
        return data, response, content
        
    def expectResponse(self, path, method, body=None, expected=None, pathVariables=None, params=None):
        
        data, response, content = self.request(path, method, body, pathVariables, params)
        errors = []
        
        if expected != None:
            for k in expected:
                v = expected[k]
                e = self.getError(data, k, v)
                if e != None:
                    errors.append(k)
                
        if len(errors) > 0:
            print("Errors found for uri: " + method + " " + self.location + " " + path)
            if pathVariables != None:
                print("Path variables: " + str(pathVariables))
            if params != None:
                print("Params: " + str(params))
            if body != None:
                print("Body: " + str(body))
            print("JSON Response: " + json.dumps(data))
            for e in errors:
                print("Expected " + str(e) + ":" + str(expected[e]))
            PyRequest.ERROR_COUNT += 1
        else:
            #print("Success for path: " + method + " " + path)
            None
        return data
    
    def getError(self, data, k, v):
        if v == None:
            return
        elif v == self.EXISTS:
            if k not in data:
                return k
        elif v == self.NOT_EXISTS:
            if k in data:
                return k
        elif isinstance(v, tuple) and len(v) == 2:
            d = data[k]
            if d < v[0] or d > v[1]:
                return k
        elif isinstance(v, list):
            if k in data and isinstance(data[k], list):
                for ll in v:
                    found = False
                    if ll in data[k]:
                        found = True
                    else:
                        for dk in data[k]:
                            if v == dk:
                                found = True
                            elif isinstance(dk, dict):
                                matchesDict = True
                                for dkk in dk:
                                    z = self.getError(dk, dkk, ll[dkk])
                                    if z != None:
                                        matchesDict = False
                                if matchesDict:
                                    found = True
                            else:
                                return k
                    if not found:
                        return k
                return None
            else:
                for ll in v:
                    z = self.getError(data, k, ll)
                    if z == None:
                        return None
                return k
        else:
            if (k not in data):
                return k
            elif (data[k] != v):
                if isinstance(v, dict) and isinstance(data[k], dict):
                    elist = []
                    for nk in v:
                        nv = v[nk]
                        z = self.getError(data[k], nk, nv)
                        if z != None:
                            if isinstance(z, list):
                                for zi in z:
                                    elist.append(zi)
                            else:
                                elist.append(z)
                    if len(elist) == 0:
                        return None
                    else:
                        return elist
                elif isinstance(data[k], list):
                    for dk in data[k]:
                        if isinstance(v, dict):
                            founds = True
                            for l in v:
                                lv = v[l]
                                z = self.getError(dk, l, lv)
                                if z != None:
                                    founds = False
                            if founds:
                                return None
                        elif v == dk:
                            return None
                return k
        return None
