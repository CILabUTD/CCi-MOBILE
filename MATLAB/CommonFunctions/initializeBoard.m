function boardHandle = initializeBoard(p)
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here


boardHandle = class_interface(8,5000000);  % 8 is the COM port on my machine - Update this to match the COM port of the interface card on your machine
% Go to Device Manager under COM ports and find the port number on your machine. 
% Successful installation of the board will show two COM port devices.
% COM port will typically be the lower of the two numbers. 
% (If one does not work, change to the other number and try).
% 5000000 is the USB-UART serial baud rate = 5MHz. 

test_output_buffer = UART_start_buffer; % Start-up buffer with standard null values;
Write(boardHandle, test_output_buffer,516); % to get started
%give Success output;
end

