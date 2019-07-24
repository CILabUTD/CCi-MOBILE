function  x  = AudioSignal( inputWaveFilePath, lr_select )
%UNTITLED Summary of this function goes here
%   Detailed explanation goes here

if ischar(inputWaveFilePath)	% Assume it is the name of a wave file
		[x, fs] = audioread(inputWaveFilePath);
        sz = size(x);
        if (sz(2)~=1)
            % then this is a stereo signal
            if (strcmp(lr_select, 'left')==1)
                % left channel
                x = x(:,1);
            else
                % right channel
                x = x(:,2);
            end
        end
        
       if (fs~=16000)
            x = resample(x, 16000, fs);
            fs=16000;
            %errordlg('Signal is resampled at 16000 Hz');
        end
            
		% Allow small sample rate differences:
%		rate_ratio = fs / p.audio_sample_rate;
% 		if (rate_ratio   > p.wav_sample_rate_tolerance)...
% 		|  (1/rate_ratio > p.wav_sample_rate_tolerance)
% 			x = resample(x, p.audio_sample_rate, fs);
% 		end
end
    

end

