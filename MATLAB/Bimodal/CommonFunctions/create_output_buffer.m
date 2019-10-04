function [ outputBuffer ] = create_output_buffer(p )
%	Creates output buffer in the specific format needed for stimulation
%   Populates timing parameters from the subject's MAP
electric_only_buffer_size = 516;
bimodal_buffer_size = 1032;
outputBuffer = uint8(zeros(1,bimodal_buffer_size));
header_hex = {'88', 'fe', '05', '01', '04', 'fc'}; header_dec = uint8(hex2dec(header_hex));
a=1;
outputBuffer(1:6) = header_dec;
a=259;
outputBuffer(259:264) = header_dec;
left_map_exists = 0; right_map_exists = 0;
left_and_right_maps_exist = 0;

left_map_exists = isfield(p,'Left');
right_map_exists = isfield(p,'Right');
if (left_map_exists==1)&&(right_map_exists==1)
    %Both maps exist
    left_and_right_maps_exist = 1;
end
if (left_map_exists==0)&&(right_map_exists==0)
    %No maps exist
    errordlg('No ear is active');
end

if (left_and_right_maps_exist ==1)
    % Populate output buffer with parameters from both sides
    a=381;
    outputBuffer(381)=uint8(p.Left.StimulationModeCode); %mode left
    outputBuffer(382)=uint8(p.Right.StimulationModeCode); %mode right
    outputBuffer(383)=p.Left.pulseWidth(2); %left pulsewidth high[15:8]
    outputBuffer(384)=p.Left.pulseWidth(1); %left pulsewidth low[7:0]
    outputBuffer(385)=p.Right.pulseWidth(2); %right pulsewidth high[15:8]
    outputBuffer(386)=p.Right.pulseWidth(1); %right pulsewidth low[7:0]
    a=507;
    outputBuffer(507)=p.Left.pulsesPerFrame(2); %left pulsesPerFrame high[15:8]
    outputBuffer(508)=p.Left.pulsesPerFrame(1); %left pulsesPerFrame low[7:0]
    outputBuffer(509)=p.Right.pulsesPerFrame(2); %right pulsesPerFrame high[15:8]
    outputBuffer(510)=p.Right.pulsesPerFrame(1); %right pulsesPerFrame low[7:0]
    outputBuffer(511)=p.Left.nRFcycles(2); %left pulsesPerFrame high[15:8]
    outputBuffer(512)=p.Left.nRFcycles(1); %left pulsesPerFrame low[7:0]
    outputBuffer(513)=p.Right.nRFcycles(2); %right pulsesPerFrame high[15:8]
    outputBuffer(514)=p.Right.nRFcycles(1); %right pulsesPerFrame low[7:0]
end

if (left_and_right_maps_exist ==0)&&(left_map_exists ==1)
    % only left map exists % Use same parameters for left and right ears
    a=381;
    outputBuffer(381)=uint8(p.Left.StimulationModeCode); %mode left
    outputBuffer(382)=uint8(p.Left.StimulationModeCode); %mode right
    outputBuffer(383)=p.Left.pulseWidth(2); %left pulsewidth high[15:8]
    outputBuffer(384)=p.Left.pulseWidth(1); %left pulsewidth low[7:0]
    outputBuffer(385)=p.Left.pulseWidth(2); %right pulsewidth high[15:8]
    outputBuffer(386)=p.Left.pulseWidth(1); %right pulsewidth low[7:0]
    a=507;
    outputBuffer(507)=p.Left.pulsesPerFrame(2); %left pulsesPerFrame high[15:8]
    outputBuffer(508)=p.Left.pulsesPerFrame(1); %left pulsesPerFrame low[7:0]
    outputBuffer(509)=p.Left.pulsesPerFrame(2); %right pulsesPerFrame high[15:8]
    outputBuffer(510)=p.Left.pulsesPerFrame(1); %right pulsesPerFrame low[7:0]
    outputBuffer(511)=p.Left.nRFcycles(2); %left pulsesPerFrame high[15:8]
    outputBuffer(512)=p.Left.nRFcycles(1); %left pulsesPerFrame low[7:0]
    outputBuffer(513)=p.Left.nRFcycles(2); %right pulsesPerFrame high[15:8]
    outputBuffer(514)=p.Left.nRFcycles(1); %right pulsesPerFrame low[7:0]
end

if (left_and_right_maps_exist ==0)&&(right_map_exists ==1)
    % only right map exists % Use same parameters for left and right ears
    a=381;
    outputBuffer(381)=uint8(p.Right.StimulationModeCode); %mode left
    outputBuffer(382)=uint8(p.Right.StimulationModeCode); %mode right
    outputBuffer(383)=p.Right.pulseWidth(2); %left pulsewidth high[15:8]
    outputBuffer(384)=p.Right.pulseWidth(1); %left pulsewidth low[7:0]
    outputBuffer(385)=p.Right.pulseWidth(2); %right pulsewidth high[15:8]
    outputBuffer(386)=p.Right.pulseWidth(1); %right pulsewidth low[7:0]
    a=507;
    outputBuffer(507)=p.Right.pulsesPerFrame(2); %left pulsesPerFrame high[15:8]
    outputBuffer(508)=p.Right.pulsesPerFrame(1); %left pulsesPerFrame low[7:0]
    outputBuffer(509)=p.Right.pulsesPerFrame(2); %right pulsesPerFrame high[15:8]
    outputBuffer(510)=p.Right.pulsesPerFrame(1); %right pulsesPerFrame low[7:0]
    outputBuffer(511)=p.Right.nRFcycles(2); %left pulsesPerFrame high[15:8]
    outputBuffer(512)=p.Right.nRFcycles(1); %left pulsesPerFrame low[7:0]
    outputBuffer(513)=p.Right.nRFcycles(2); %right pulsesPerFrame high[15:8]
    outputBuffer(514)=p.Right.nRFcycles(1); %right pulsesPerFrame low[7:0]
end
        outputBuffer(516) = uint8(hex2dec('bb'));
        outputBuffer(517) = uint8(hex2dec('ab'));
        outputBuffer(1032) = uint8(hex2dec('ef'));


