function [c,ceq]= ThreePhaseLossConstraint3(x,Num_bus,Bus_ref,Limit_Slack,Q_Lim,Ybus,connection)
global P_dim Q_dim P_gen;

%% bus voltage limit (inequality constraint)

for I=1:Num_bus
    c((I-1)*3+1:(I-1)*3+3,1)=x((I-1)*3+1:(I-1)*3+3)'-1.05*ones(3,1);
%     if I>62
%         c((I-1)*3+1:(I-1)*3+3,1)=x((I-1)*3+1:(I-1)*3+3)'-[0;1.05;0];
%     end
end
for I=Num_bus+1:2*Num_bus
    c((I-1)*3+1:(I-1)*3+3,1)=0.95*ones(3,1)-x((I-Num_bus-1)*3+1:(I-Num_bus-1)*3+3)';
%     if I>62
%         c((I-1)*3+1:(I-1)*3+3,1)=[0;0.95;0]-x((I-Num_bus-1)*3+1:(I-Num_bus-1)*3+3)';
%     end
end


for I=2*Num_bus+1:3*Num_bus 
    c((I-1)*3+1:(I-1)*3+3,1)=x((I-Num_bus-1)*3+1:(I-Num_bus-1)*3+3)'-2*pi*ones(3,1);
end
for I=3*Num_bus+1:4*Num_bus
    c((I-1)*3+1:(I-1)*3+3,1)=zeros(3,1)-x((I-2*Num_bus-1)*3+1:(I-2*Num_bus-1)*3+3)';
end


%% reactive power limit (inequality constraint)
for I=4*Num_bus+1:5*Num_bus
    c((I-1)*3+1:(I-1)*3+3,1)=x((I-2*Num_bus-1)*3+1:(I-2*Num_bus-1)*3+3)'-Q_Lim(I-4*Num_bus,4:6)';
end

for I=5*Num_bus+1:6*Num_bus
    c((I-1)*3+1:(I-1)*3+3,1)=Q_Lim(I-5*Num_bus,1:3)'-x((I-3*Num_bus-1)*3+1:(I-3*Num_bus-1)*3+3)';
end   

%% Bus angle constraint

for I=6*Num_bus+1:7*Num_bus
    c((I-1)*3+1:(I-1)*3+3,1)=(22*pi/36)*ones(3,1)-[abs(x((I-5*Num_bus-1)*3+2)-x((I-5*Num_bus-1)*3+1));
                                                   abs(x((I-5*Num_bus-1)*3+3)-x((I-5*Num_bus-1)*3+2));
                                                   abs(x((I-5*Num_bus-1)*3+1)-x((I-5*Num_bus-1)*3+3));];
end 

for I=7*Num_bus+1:8*Num_bus
    c(7*3*Num_bus+I-7*Num_bus,1)=[abs(x((I-6*Num_bus-1)*3+2)-x((I-6*Num_bus-1)*3+1))+...
                                    abs(x((I-6*Num_bus-1)*3+3)-x((I-6*Num_bus-1)*3+2))+...
                                    abs(x((I-6*Num_bus-1)*3+1)-x((I-6*Num_bus-1)*3+3))]-(2*pi/3)-(380*2*pi/360);
end     

for I=8*Num_bus+1:9*Num_bus
    c(7*3*Num_bus+Num_bus+I-8*Num_bus,1)=(340*2*pi/360)-[abs(x((I-7*Num_bus-1)*3+2)-x((I-7*Num_bus-1)*3+1))+...
                                                            abs(x((I-7*Num_bus-1)*3+3)-x((I-7*Num_bus-1)*3+2))+...
                                                            abs(x((I-7*Num_bus-1)*3+1)-x((I-7*Num_bus-1)*3+3))-(2*pi/3)];
end  

% for I=6*Num_bus+1:7*Num_bus
%     c((I-1)*3+1:(I-1)*3+3,1)=(20*pi/36)*ones(3,1)-[abs(x((I-5*Num_bus-1)*3+2)-x((I-5*Num_bus-1)*3+1));
%                                                    abs(x((I-5*Num_bus-1)*3+3)-x((I-5*Num_bus-1)*3+2));
%                                                    abs(x((I-5*Num_bus-1)*3+1)-x((I-5*Num_bus-1)*3+3));];
% end 
% 
% for I=7*Num_bus+1:8*Num_bus
%     c(7*3*Num_bus+I-7*Num_bus,1)=[abs(x((I-6*Num_bus-1)*3+2)-x((I-6*Num_bus-1)*3+1))+...
%                                     abs(x((I-6*Num_bus-1)*3+3)-x((I-6*Num_bus-1)*3+2))+...
%                                     abs(x((I-6*Num_bus-1)*3+1)-x((I-6*Num_bus-1)*3+3))]-(2*pi/3)-(420*2*pi/360);
% end     
% 
% for I=8*Num_bus+1:9*Num_bus
%     c(7*3*Num_bus+Num_bus+I-8*Num_bus,1)=(300*2*pi/360)-[abs(x((I-7*Num_bus-1)*3+2)-x((I-7*Num_bus-1)*3+1))+...
%                                                             abs(x((I-7*Num_bus-1)*3+3)-x((I-7*Num_bus-1)*3+2))+...
%                                                             abs(x((I-7*Num_bus-1)*3+1)-x((I-7*Num_bus-1)*3+3))-(2*pi/3)];
% end 

%% active power limit of slack generator (inequality constraint)
c(7*3*Num_bus+2*Num_bus+1:7*3*Num_bus+2*Num_bus+3,1)=x(9*Num_bus+1:9*Num_bus+3)'-Limit_Slack(4:6)';

c(7*3*Num_bus+2*Num_bus+1+3:7*3*Num_bus+2*Num_bus+3+3,1)=Limit_Slack(1:3)'-x(9*Num_bus+1:9*Num_bus+3)';
c(7*3*Num_bus+2*Num_bus+3+3+1,1)=x(3*Num_bus+1)-x(3*Num_bus+2);
c(7*3*Num_bus+2*Num_bus+3+3+2,1)=x(3*Num_bus+2)-x(3*Num_bus+3);
c(7*3*Num_bus+2*Num_bus+3+3+3,1)=x(3*Num_bus+1)-0.01;

c(7*3*Num_bus+2*Num_bus+3+3+3,1)=x(3*Num_bus+1)-0.02;

% if sum(P_dim(:,1))-sum(P_gen(:,1))>0
%     c(7*3*Num_bus+2*Num_bus+3+3+3+1,1)=sum(P_dim(:,1))-sum(P_gen(:,1))-x(9*Num_bus+1);
% end
% if sum(P_dim(:,2))-sum(P_gen(:,2))>0
%     c(7*3*Num_bus+2*Num_bus+3+3+3+2,1)=sum(P_dim(:,2))-sum(P_gen(:,2))-x(9*Num_bus+2);
% end
% if sum(P_dim(:,3))-sum(P_gen(:,3))>0
%     c(7*3*Num_bus+2*Num_bus+3+3+3+3,1)=sum(P_dim(:,3))-sum(P_gen(:,3))-x(9*Num_bus+3);
% end
    
% c(7*3*Num_bus+2*Num_bus+3+3+4,1)=x(1)-1;
% %% active power limit of slack generator (inequality constraint)
% c(7*3*Num_bus+Num_bus+1:7*3*Num_bus+Num_bus+3,1)=x(9*Num_bus+1:9*Num_bus+3)'-Limit_Slack(4:6)';
% 
% c(7*3*Num_bus+Num_bus+1+3:7*3*Num_bus+Num_bus+3+3,1)=Limit_Slack(1:3)'-x(9*Num_bus+1:9*Num_bus+3)';

%% load flow equations (equality constraint)
% active power mismatch

ss=zeros(3,Num_bus);
for ii=1:Num_bus
    jj=[ii; connection(connection(:,2)==ii,1); connection(connection(:,1)==ii,2)];
    for j=1:length(jj)
%         x((ii-1)*3+1:(ii-1)*3+3)'
%         x((jj(j)-1)*3+1:(jj(j)-1)*3+3)'
        ss(1:3,ii)=ss(1:3,ii)+x((ii-1)*3+1:(ii-1)*3+3)'.*x((jj(j)-1)*3+1:(jj(j)-1)*3+3)'.*...
            (real(cell2mat(Ybus(ii,jj(j))))*cos(x((ii+Num_bus-1)*3+1:(ii+Num_bus-1)*3+3)'-x((jj(j)+Num_bus-1)*3+1:(jj(j)+Num_bus-1)*3+3)')+...
             imag(cell2mat(Ybus(ii,jj(j))))*sin(x((ii+Num_bus-1)*3+1:(ii+Num_bus-1)*3+3)'-x((jj(j)+Num_bus-1)*3+1:(jj(j)+Num_bus-1)*3+3)'));
    end
    if ii == Bus_ref
        ceq(1:3,ii)=x(3*3*Num_bus+1:3*3*Num_bus+3)'-P_dim(ii,:)'-ss(1:3,ii);
    else
        ceq(1:3,ii)=P_gen(ii,:)'-P_dim(ii,:)'-ss(1:3,ii);
    end
end
% reactive power mismatch
ss=zeros(3,Num_bus);
for ii=1:Num_bus
    jj=[ii; connection(connection(:,2)==ii,1); connection(connection(:,1)==ii,2)];
    for j=1:length(jj)
        ss(1:3,ii)=ss(1:3,ii)+x((ii-1)*3+1:(ii-1)*3+3)'.*x((jj(j)-1)*3+1:(jj(j)-1)*3+3)'.*...
            (real(cell2mat(Ybus(ii,jj(j))))*sin(x((ii+Num_bus-1)*3+1:(ii+Num_bus-1)*3+3)'-x((jj(j)+Num_bus-1)*3+1:(jj(j)+Num_bus-1)*3+3)')-...
             imag(cell2mat(Ybus(ii,jj(j))))*cos(x((ii+Num_bus-1)*3+1:(ii+Num_bus-1)*3+3)'-x((jj(j)+Num_bus-1)*3+1:(jj(j)+Num_bus-1)*3+3)'));
    end
    ceq(1:3,ii+Num_bus)=x((ii+2*Num_bus-1)*3+1:(ii+2*Num_bus-1)*3+3)'-Q_dim(ii,:)'-ss(1:3,ii);
end

%ceq=[];
ceq(1:3,2*Num_bus+1)=x(1:3)-1;

%ceq(1:3,2*Num_bus+1)=[x(3*Num_bus+1);x(3*Num_bus+2)-(2*pi/3);x(3*Num_bus+3)+(2*pi/3)];
%save('C:\Users\malekpour\Desktop\Optimal Power Flow\Constraints1.mat', 'c','ceq')



