function [xfinal] = SGJMAP_Postfilt_SE(x,Srate,muu,ve)
%%--------------Implements Proposed SGJMAP based Speech Enhancement with post
%%processing to reduce musical noise
% Usage: x- Noisy Speech
%        Srate - Sampling Frequency
%        beta - Tradeoff parameter
% 
% Author: Chandan K A Reddy
%
% Copyright (c) 2017 by Chandan K A Reddy
% =============== Initialize variables ===============
SPU = 0;
len=floor(20*Srate/1000); % Frame size in samples
if rem(len,2)==1, len=len+1; end;
PERC=50; % window overlap in percent of frame size
len1=floor(len*PERC/100);
len2=len-len1;

win=hanning(len);  % define window
win = win*len2/sum(win);  % normalize window for equal level output 

% Noise magnitude calculations - assuming that the first 6 frames is noise/silence
nFFT=2*len; % Number of FFT points

j=1;
noise_mean=zeros(nFFT,1);
noise_pow = zeros(nFFT,1);
% Initial noise power estimation
for k=1:6
    noise_mean=noise_mean+abs(fft(win.*x(j:j+len-1),nFFT));
    j=j+len;
end
noise_mu=noise_mean/6;
noise_mu2=noise_mu.^2;

%--- allocate memory and initialize various variables

k=1;
img=sqrt(-1);
x_old=zeros(len1,1);
Nframes=floor(length(x)/len2)-1;
xfinal=zeros(Nframes*len2,1);

% --------------- Initialize parameters ------------
%
k=1;
aa=0.98;
eta= 0.15;
%mu=0.98;
%c=sqrt(pi)/2;
qk=0.3;
qkr=(1-qk)/qk;
ksi_min=10^(-25/10); 
count = 0;
%===============================  Start Processing =======================================================
% Frame based
for n=1:Nframes
H = zeros(nFFT,1);
    tic
    insign=win.*x(k:k+len-1);

    %--- Take fourier transform of  frame
    
    spec=fft(insign,nFFT);
    nsig=abs(spec); % compute the magnitude
    sig2=nsig.^2; % Compute power

    gammak=min(sig2./noise_mu2,40);  % posteriori SNR
    if n==1
        ksi=aa+(1-aa)*max(gammak-1,0);
        ensig = nsig;
    else
        ksi=aa*Xk_prev./noise_mu2 + (1-aa)*max(gammak-1,0);     
        % decision-direct estimate of a priori SNR
        ksi=max(ksi_min,ksi);  % limit ksi to -25 dB
    end

    log_sigma_k= gammak.* ksi./ (1+ ksi)- log(1+ ksi); 
    vad_decision= sum( log_sigma_k)/nFFT;    
    if (vad_decision< eta) % noise on
        %noise_mu2= mu* noise_mu2+ (1- mu)* sig2;
        noise_pow = noise_pow + noise_mu2;
        count = count+1;
    end
    noise_mu2 = noise_pow./count;
    
    vk=ksi.*gammak./(1+ksi);
    % SG JMAP Gain function
    hw=0.5-muu/(4*sqrt(ksi))+sqrt((0.5-2.5/(4*sqrt(ksi))).^2 + ve/(2*gammak));
    
    % --- estimate speech presence probability
    %
    
%     if SPU==1
%         evk=exp(vk);
%         Lambda=qkr*evk./(1+ksi);
%         pSAP=Lambda./(1+Lambda);
%        sig=sig.*hw.*pSAP;
%     else
%         sig1=sig.*hw;
%     end
    

%% Musical noise suppression
    PR = sum(abs(ensig.^2))/sum(abs(nsig.^2));
    
    if(PR>=0.4)
        PRT = 1;
    else
        PRT = PR;
    end
    
    if(PRT == 1)
        N=1;
    else
        N = 2*round((1-PRT/0.4)*10)+1;
    end
    
    H(1:N) = 1/N;
    HPF = conv(H,abs(hw));
    ensig = nsig.*HPF(1:length(nsig));
    Xk_prev=ensig.^2;  % save for estimation of a priori SNR in next frame

    xi_w= ifft( ensig .* exp(img*angle(spec)),nFFT);

    xi_w= real( xi_w);

    xfinal(k:k+ len2-1)= x_old+ xi_w(1:len1);
    x_old= xi_w(len1+ 1: len);

    k=k+len2;
   % jmp_time(n) = toc;
end 
%========================================================================================
% jmptim = mean(jmp_time)
% count
% Nframes
%wavwrite(xfinal,Srate,16,outfile);