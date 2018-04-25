function [c,ceq]= LossConstraints1(x,Num_bus,Num_gen,Bus_ref,Limit_Slack,Gen_bus,Q_Lim,Q_dim,P_dim,P_gen,Ybus_abs,Ybus_ang,v,V_angle)
%% reactive power limit (inequality constraint)
for I=1:Num_gen
    c(I,1)=x(I)-Q_Lim(I,2);
end
for I=Num_gen+1:2*Num_gen
    c(I,1)=Q_Lim(I-Num_gen,1)-x(I-Num_gen);
end

%% active power limit of slack generator (inequality constraint)
c(2*Num_gen+1,1)=x(Num_gen+1)-Limit_Slack(1,2);
c(2*Num_gen+2,1)=Limit_Slack(1,1)-x(Num_gen+1);

%% bus voltage limit (inequality constraint)
k=1;
j=Num_gen+2;
I=2*Num_gen+3;
while I<= (2*Num_bus+2*Num_gen+2)
    c(I,1)=x(j)-v(k,2);
    I=I+1;
    c(I,1)=v(k,1)-x(j);
    I=I+1;
    j=j+1;
    k=k+1;
end
% c(2*Num_gen+3,1)= x(Num_gen+2)-v(1,2)-0.005;  
% c(2*Num_gen+4,1)= -x(Num_gen+2)+v(1,1)-0.005;  
% c(2*Num_bus+2*Num_gen+2+1)=x(Num_bus+Num_gen+1+Bus_ref)-V_angle-0.005;
% c(2*Num_bus+2*Num_gen+2+2)=-x(Num_bus+Num_gen+1+Bus_ref)+V_angle-0.005;

%% load flow equations (equality constraint)
% active power mismatch
ss=zeros(Num_bus,1);
for ii=1:Num_bus
    for j=1:Num_bus
        s(ii)=ss(ii)+x(Num_gen+1+ii)*x(Num_gen+1+j)*Ybus_abs(ii,j)*cos(Ybus_ang(ii,j)+x(Num_gen+Num_bus+1+j)-x(Num_gen+Num_bus+1+ii));
        ss(ii)=s(ii);
    end
end
w=1;
for ii=1:Num_bus
    if Gen_bus(ii,1)==1
        if ii == Bus_ref
            ceq(ii,1)=x(Num_gen+1)-P_dim(ii)-s(ii);
        else
            ceq(ii,1)=P_gen(ii,1)-P_dim(ii)-s(ii);
        end
    else
        ceq(ii,1)=-P_dim(ii)-s(ii);
    end
end

% reactive power mismatch
SS=zeros(Num_bus,1);
for ii=1:Num_bus
    for j=1:Num_bus
        S(ii)=SS(ii)+x(Num_gen+1+ii)*x(Num_gen+1+j)*Ybus_abs(ii,j)*sin(Ybus_ang(ii,j)+x(Num_gen+Num_bus+1+j)-x(Num_gen+Num_bus+1+ii));
        SS(ii)=S(ii);
    end
end
W=1;
for ii=1:Num_bus
    if Gen_bus(ii,1)==1
        ceq(ii+Num_bus,1)=x(W)-Q_dim(ii)-S(ii);
        W=W+1;
    else
        ceq(ii+Num_bus,1)=-Q_dim(ii)-S(ii);
    end
end

% angle of reference bus
ceq(2*Num_bus+1,1)=x(Num_gen+1+Num_bus+Bus_ref)-V_angle;
% ceq 
% c
% save('C:\Users\malekpour\Desktop\Optimal Power Flow\Constraints1.mat', 'c','ceq')
