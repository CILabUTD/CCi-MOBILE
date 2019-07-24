function [Qout] = originalLGFfunction(log_alpha,BaseLevel,SatLevel,Q)
% ---------------------- LGF_Q_diff ---------------------------- %
% Q_diff = LGF_Q_diff(log_alpha,Q,BaseLevel,SaturationLevel);
% out    = LGF_Q_diff(log_alpha,Q,base_level,sat_level)
    alpha = exp(log_alpha);
    % -------------------- LGF_Q ------------------------------- %
    % out = LGF_Q(alpha, base_level, sat_level) - Q;
    % q = LGF_Q(alpha, base_level, sat_level)
        mini.lgf_alpha  = alpha;
        mini.base_level = BaseLevel;
        mini.sat_level  = SatLevel;
        mini.sub_mag    = 0;
        input_level     = SatLevel/sqrt(10);
        % ------------- LGF_proc --------------------------- %
        % p = LGF_proc(p, input_level);
        % p = LGF_proc(p, input_level)
            r       = (input_level-BaseLevel)/(SatLevel-BaseLevel);
            sat     = r > 1;
            r(sat)  = 1;
            sub     = r < 0;
            r(sub)  = 0;
            v       = log(1 + mini.lgf_alpha * r) / log(1 + mini.lgf_alpha);
            v(sub)  = -1e-10;
            mini    = v;
        q    = 100 * (1 - mini);
    Qout = q - Q;
end % function end 'originalLGFalpha.m'
% ---------------------------------------------------------------------- %