#include "mex.h"
#include "class_handle.hpp"

#include <windows.h>
using namespace std;
#include <iostream>

#define FC_DTRDSR       0x01
#define FC_RTSCTS       0x02
#define FC_XONXOFF      0x04
#define ASCII_BEL       0x07
#define ASCII_BS        0x08
#define ASCII_LF        0x0A
#define ASCII_CR        0x0D
#define ASCII_XON       0x11
#define ASCII_XOFF      0x13

class CSerial
{

public:
CSerial();
~CSerial();

BOOL Open(int nPort = 15, int nBaud = 5000000);
BOOL Close(void);

int ReadData(void *, int);
int SendData(const char *, int);
int ReadDataWaiting(void);

BOOL IsOpened(void){ return(m_bOpened); }

protected:
BOOL WriteCommByte(unsigned char);

HANDLE m_hIDComDev;
OVERLAPPED m_OverlappedRead, m_OverlappedWrite;
BOOL m_bOpened;

};

/*********************/

CSerial::CSerial()
{

	memset(&m_OverlappedRead, 0, sizeof(OVERLAPPED));
	memset(&m_OverlappedWrite, 0, sizeof(OVERLAPPED));
	m_hIDComDev = NULL;
	m_bOpened = FALSE;

}

CSerial::~CSerial()
{
    #ifdef _WIN32
      mexPrintf("Closing\n");
    #else
      cout << "Closing"<<endl;
    #endif
	Close();

}

BOOL CSerial::Open(int nPort, int nBaud)
{

	if (m_bOpened) return(TRUE);

	TCHAR szPort[15];//L"\\\\.\\COM19";
	//wchar_t szComParams[50];
	DCB dcb;

	wsprintf(szPort, TEXT("\\\\.\\COM%d"), nPort);
	m_hIDComDev = CreateFile(szPort, GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL | FILE_FLAG_OVERLAPPED, NULL);
	if (m_hIDComDev == NULL) return(FALSE);

	memset(&m_OverlappedRead, 0, sizeof(OVERLAPPED));
	memset(&m_OverlappedWrite, 0, sizeof(OVERLAPPED));

	COMMTIMEOUTS CommTimeOuts;
	CommTimeOuts.ReadIntervalTimeout = 0xFFFFFFFF;
	CommTimeOuts.ReadTotalTimeoutMultiplier =250;
	CommTimeOuts.ReadTotalTimeoutConstant = 250;
	CommTimeOuts.WriteTotalTimeoutMultiplier = 0;
	CommTimeOuts.WriteTotalTimeoutConstant = 5000;
	SetCommTimeouts(m_hIDComDev, &CommTimeOuts);

	//wsprintf(szComParams, L"COM%d:%d,n,8,1", nPort, nBaud);

	m_OverlappedRead.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);
	m_OverlappedWrite.hEvent = CreateEvent(NULL, TRUE, FALSE, NULL);

	dcb.DCBlength = sizeof(DCB);
	GetCommState(m_hIDComDev, &dcb);
	dcb.BaudRate = nBaud;
	dcb.Parity = NOPARITY;
	dcb.StopBits = TWOSTOPBITS;
	dcb.ByteSize = 8;
	unsigned char ucSet;
	ucSet = (unsigned char)((FC_RTSCTS & FC_DTRDSR) != 0);
	ucSet = (unsigned char)((FC_RTSCTS & FC_RTSCTS) != 0);
	ucSet = (unsigned char)((FC_RTSCTS & FC_XONXOFF) != 0);
	if (!SetCommState(m_hIDComDev, &dcb) || !SetupComm(m_hIDComDev, 10000, 10000) ||	m_OverlappedRead.hEvent == NULL ||	m_OverlappedWrite.hEvent == NULL){
		DWORD dwError = GetLastError();
		if (m_OverlappedRead.hEvent != NULL) CloseHandle(m_OverlappedRead.hEvent);
		if (m_OverlappedWrite.hEvent != NULL) CloseHandle(m_OverlappedWrite.hEvent);
		CloseHandle(m_hIDComDev);
		return(FALSE);
	}

	m_bOpened = TRUE;

	return(m_bOpened);

}

BOOL CSerial::Close(void)
{

	if (!m_bOpened || m_hIDComDev == NULL) return(TRUE);
	
	if (m_OverlappedRead.hEvent != NULL) CloseHandle(m_OverlappedRead.hEvent);
	if (m_OverlappedWrite.hEvent != NULL) CloseHandle(m_OverlappedWrite.hEvent);
	CloseHandle(m_hIDComDev);
	m_bOpened = FALSE;
	m_hIDComDev = NULL;

	return(TRUE);

}

BOOL CSerial::WriteCommByte(unsigned char ucByte)
{
	BOOL bWriteStat;
	DWORD dwBytesWritten;

	bWriteStat = WriteFile(m_hIDComDev, (LPSTR)&ucByte, 1, &dwBytesWritten, &m_OverlappedWrite);
	if (!bWriteStat && (GetLastError() == ERROR_IO_PENDING)){
	if (WaitForSingleObject(m_OverlappedWrite.hEvent, 1000)) dwBytesWritten = 0;
	else{
		GetOverlappedResult(m_hIDComDev, &m_OverlappedWrite, &dwBytesWritten, FALSE);
		m_OverlappedWrite.Offset += dwBytesWritten;
	}
}

return(TRUE);

}

int CSerial::SendData(const char *buffer, int size)
{

	if (!m_bOpened || m_hIDComDev == NULL) return(0);
	//unsigned long done;
	//DWORD dwBytesWritten = 0;
	BOOL bWriteStat;
	DWORD dwBytesWritten;
	/*int i;
	for (i = 0; i<size; i++){
	WriteCommByte(buffer[i]);
	dwBytesWritten++;
	}*/
	bWriteStat = WriteFile(m_hIDComDev, buffer, size, &dwBytesWritten, &m_OverlappedWrite);
	if (!bWriteStat && (GetLastError() == ERROR_IO_PENDING)){
	if (WaitForSingleObject(m_OverlappedWrite.hEvent, 1000)) dwBytesWritten = 0;
	else{
		GetOverlappedResult(m_hIDComDev, &m_OverlappedWrite, &dwBytesWritten, FALSE);
		m_OverlappedWrite.Offset += dwBytesWritten;
	}
}

return((int)dwBytesWritten);

}

int CSerial::ReadDataWaiting(void)
{

	if (!m_bOpened || m_hIDComDev == NULL) return(0);

	DWORD dwErrorFlags;
	COMSTAT ComStat;

	ClearCommError(m_hIDComDev, &dwErrorFlags, &ComStat);

	return((int)ComStat.cbInQue);

}

int CSerial::ReadData(void *buffer, int limit)
{

	if (!m_bOpened || m_hIDComDev == NULL) return(0);

	BOOL bReadStatus;
	DWORD dwBytesRead, dwErrorFlags;
	COMSTAT ComStat;

	ClearCommError(m_hIDComDev, &dwErrorFlags, &ComStat);
	if (!ComStat.cbInQue) return(0);

	dwBytesRead = (DWORD)ComStat.cbInQue;
	if (limit < (int)dwBytesRead) dwBytesRead = (DWORD)limit;

	bReadStatus = ReadFile(m_hIDComDev, buffer, dwBytesRead, &dwBytesRead, &m_OverlappedRead);
	if (!bReadStatus){
		if (GetLastError() == ERROR_IO_PENDING){
			WaitForSingleObject(m_OverlappedRead.hEvent, 2000);
			return((int)dwBytesRead);
		}
		return(0);
	}

	return((int)dwBytesRead);

}



/*************************
 ***** Main function ***** 
 *************************/


void mexFunction(int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[])
{	
    // Get the command string
    char cmd[64];
    CSerial *obj;	
    if (nrhs < 1 || mxGetString(prhs[0], cmd, sizeof(cmd)))
		mexErrMsgTxt("First input should be a command string less than 64 characters long.");
        
    // New
    if (!strcmp("new", cmd)) {
        // Check parameters
        int ip;
        int baud;
        if (nrhs == 3){
            ip = mxGetScalar(prhs[1]);
            baud = mxGetScalar(prhs[2]);
            #ifdef _WIN32
              mexPrintf("After setting the object's data to your input:\n%d\t%d\n",ip,baud);
            #else
              cout << "After setting the object's data to your input:\n"<<ip <<"\t"<<baud<<endl;
            #endif
        }    
        if (nlhs != 1)
            mexErrMsgTxt("New: One output expected.");
        // Return a handle to a new C++ instance
		obj = new CSerial;
		obj->Open(ip,baud);
        plhs[0] = convertPtr2Mat<CSerial>(obj);
        return;
    }
    
    // Check there is a second input, which should be the class instance handle
    if (nrhs < 2)
		mexErrMsgTxt("Second input should be a class instance handle.");
    
    // Delete
    if (!strcmp("delete", cmd)) {
		// Destroy the C++ object
		destroyObject<CSerial>(prhs[1]);
        // Warn if other commands were ignored
        if (nlhs != 0 || nrhs != 2)
            mexWarnMsgTxt("Delete: Unexpected arguments ignored.");
        return;
    }
    
    // Get the class instance pointer from the second input
    
    
    // Call the various class methods
    // Train    
    if (!strcmp("Read", cmd)) {
        char *readBuff/*[512]*/;
        int len;
        // Check parameters
        if (nlhs != 1 || nrhs != 3)
            mexErrMsgTxt("Train: Unexpected arguments.");
		
		CSerial *dummy_instance = convertMat2Ptr<CSerial>(prhs[1]);
        len = mxGetScalar(prhs[2]);
        
		plhs[0] = mxCreateNumericMatrix(1,len,mxINT8_CLASS,mxREAL);
        readBuff = (char *) mxGetPr(plhs[0]);
        
        // Call the method
        dummy_instance->ReadData(readBuff,len);
        char t;
        for(int i=0;i<len-1;){
            t = readBuff[i];
            readBuff[i] = readBuff[i+1];
            readBuff[i+1] = t;
            i+=2;
        }
		return;
    }
    
    if (!strcmp("Wait", cmd)) {
        //char *readBuff/*[512]*/;
        int *len;
        // Check parameters
        if (nlhs != 1 || nrhs != 2)
            mexErrMsgTxt("Train: Unexpected arguments.");
		
		CSerial *dummy_instance = convertMat2Ptr<CSerial>(prhs[1]);
        //len = dummy_instance->ReadDataWaiting();
        
		plhs[0] = mxCreateNumericMatrix(1,1, mxINT32_CLASS,mxREAL);
        len = (int *)mxGetPr(plhs[0]);
        *len = dummy_instance->ReadDataWaiting();
        
        // Call the method
        //int z = dummy_instance->ReadData(readBuff,len);
		return;
    }
    
    // Test    
    if (!strcmp("Write", cmd)) {
        char *writeBuff;
        int len;
        // Check parameters
        if (nlhs != 0 || nrhs != 4)
            mexErrMsgTxt("Test: Unexpected arguments.");
			
		CSerial *dummy_instance = convertMat2Ptr<CSerial>(prhs[1]);
        writeBuff = (char *) mxGetPr(prhs[2]);
        len = mxGetScalar(prhs[3]);
		
		// Call the method
        int z = dummy_instance->SendData(writeBuff,len);
		return;
    }
    
    // Got here, so command not recognized
    mexErrMsgTxt("Command not recognized.");
}
