function outputBuffer = UART_output_buffer( stim , p)
%UNTITLED2 Summary of this function goes here
%   Detailed explanation goes here
nl = p.Left.Nmaxima; nr = p.Right.Nmaxima;
electrode_token_l = 1:nl; electrode_token_r = 1:nr;
stiml = stim.l; stimr = stim.r;
% if (numel(stiml)~=numel(electrode_token_l))
%     errordlg('numel(sine_token_l)~=numel(electrode_token_r)');
% end
% if (numel(stimr)~=numel(electrode_token_r))
%     errordlg('numel(sine_token_r)~=numel(electrode_token_r)');
% end

% ppf = uint16(64); pulsesPerFrame = typecast(ppf,'uint8');
% pw = uint16(25); pulseWidth= typecast(pw,'uint8');
% mode = 28;

ppfl = p.Left.pulses_per_frame;
ppfr = p.Right.pulses_per_frame;

lelecs = uint8(ones(1,ppfl)); relecs = uint8(ones(1,ppfr));
lamps = uint8(ones(1,ppfl)); ramps = uint8(ones(1,ppfr));

a=1;
for (i=1:ppfl/nl)
    lamps(a:a+nl-1) = stiml(i); a= a+nl;
end
a=1;
for (i=1:ppfl/nl)
    lelecs(a:a+nl-1) = electrode_token_l; a=a+nl;
end

a=1;
for (i=1:ppfr/nr)
    ramps(a:a+nr-1) = stimr(i); a= a+nr;
end
a=1;
for (i=1:ppfr/nr)
    relecs(a:a+nr-1) = electrode_token_r; a=a+nr;
end


outputBuffer = uint8(zeros(1,516));
header_hex = {'88', 'fe', '05', '01', '04', 'fc'}; header_dec = uint8(hex2dec(header_hex)); 
a=1;
outputBuffer(1:6) = header_dec;
a=7;
for (i=1:ppfl)
    outputBuffer(a) = lelecs(i);a=a+1; %left electrodes
end
a= 133;
for (i=1:ppfl)
    outputBuffer(a) = lamps(i);a=a+1; %left amplitudes
end
a=259;
outputBuffer(259:264) = header_dec;
a=265;
for (i=1:ppfr)
    outputBuffer(a) = relecs(i);a=a+1; %right electrodes
end
a=381;
outputBuffer(381)=uint8(p.Left.StimulationModeCode); %mode left
outputBuffer(382)=uint8(p.Right.StimulationModeCode); %mode right
outputBuffer(383)=p.Left.pulseWidth(2); %left pulsewidth high[15:8]
outputBuffer(384)=p.Left.pulseWidth(1); %left pulsewidth low[7:0]
outputBuffer(385)=p.Right.pulseWidth(2); %right pulsewidth high[15:8]
outputBuffer(386)=p.Right.pulseWidth(1); %right pulsewidth low[7:0]
a=391;
for (i=1:ppfr)
    outputBuffer(a) = ramps(i);a=a+1; %right amplitudes
end

a=507;
outputBuffer(507)=p.Left.pulsesPerFrame(2); %left pulsesPerFrame high[15:8]
outputBuffer(508)=p.Left.pulsesPerFrame(1); %left pulsesPerFrame low[7:0]
outputBuffer(509)=p.Right.pulsesPerFrame(2); %right pulsesPerFrame high[15:8]
outputBuffer(510)=p.Right.pulsesPerFrame(1); %right pulsesPerFrame low[7:0]
outputBuffer(511)=p.Left.nRFcycles(2); %left pulsesPerFrame high[15:8]
outputBuffer(512)=p.Left.nRFcycles(1); %left pulsesPerFrame low[7:0]
outputBuffer(513)=p.Right.nRFcycles(2); %right pulsesPerFrame high[15:8]
outputBuffer(514)=p.Right.nRFcycles(1); %right pulsesPerFrame low[7:0]



