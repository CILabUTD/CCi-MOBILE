function boardHandle = initializeBoard(p)
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here


boardHandle = class_interface(3,5000000); % 9 is the COM port on my machine - Update this to match the COM port of the interface card on your machine
                                           % 5000000 is the USB-UART serial baud rate = 5MHz. 
test_output_buffer = UART_start_buffer; % Start-up buffer with standard null values;
Write(boardHandle, test_output_buffer,1032); % to get started
%give Success output;
end

