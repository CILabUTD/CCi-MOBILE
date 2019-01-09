function outputBuffer = UART_start_buffer
% Start-up buffer to get the board started
% OutputBuffer contains null values

n = 8;
electrode_token = 1:n;
pw = uint16(25); pulseWidth= typecast(pw,'uint8');
ppf = uint16(64); pulsesPerFrame = typecast(ppf,'uint8');
mode = 28;
cycles = uint16(600); nRFcycles= typecast(cycles,'uint8');
a=1;
for (i=1:ppf/n)
    electrodes(a:a+n-1) = electrode_token; a=a+n;
end

outputBuffer = uint8(zeros(1,516));
header_hex = {'88', 'fe', '05', '01', '04', 'fc'}; header_dec = uint8(hex2dec(header_hex)); 
a=1;
outputBuffer(1:6) = header_dec;
a=7;
outputBuffer(7:7+ppf-1) = electrodes; %left electrodes
a= 133;
outputBuffer(133:248) = uint8(0);%left amplitudes
a=259;
outputBuffer(259:264) = header_dec;
a=265;
outputBuffer(265:265+ppf-1) = electrodes;%right electrodes
a=381;
outputBuffer(381)=uint8(mode); %mode left
outputBuffer(382)=uint8(mode); %mode right
outputBuffer(383)=pulseWidth(2); %left pulsewidth high[15:8]
outputBuffer(384)=pulseWidth(1); %left pulsewidth low[7:0]
outputBuffer(385)=pulseWidth(2); %right pulsewidth high[15:8]
outputBuffer(386)=pulseWidth(1); %right pulsewidth low[7:0]
a=391;
outputBuffer(391:506) =  uint8(0); %right amplitudes
a=507;
outputBuffer(507)=pulsesPerFrame(2); %left pulsesPerFrame high[15:8]
outputBuffer(508)=pulsesPerFrame(1); %left pulsesPerFrame low[7:0]
outputBuffer(509)=pulsesPerFrame(2); %right pulsesPerFrame high[15:8]
outputBuffer(510)=pulsesPerFrame(1); %right pulsesPerFrame low[7:0]
outputBuffer(511)=nRFcycles(2); %left pulsesPerFrame high[15:8]
outputBuffer(512)=nRFcycles(1); %left pulsesPerFrame low[7:0]
outputBuffer(513)=nRFcycles(2); %right pulsesPerFrame high[15:8]
outputBuffer(514)=nRFcycles(1); %right pulsesPerFrame low[7:0]



