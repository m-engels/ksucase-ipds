clc
clear all
close all
%% Initialization
iter=1;

%% This function acts as an aggregator from the Neighborhood level to substation level
%% The aggregated values i.e. PL,QL,PG are saved in Net
[Net]=Aggregate1(iter);

%%

ieee_Sub=Net.Sub; % The Substation holon is using this to build the network configuration at Substation level
Prs(:,1:3) = [ieee_Sub(:,7) ieee_Sub(:,9) ieee_Sub(:,11)]-ieee_Sub(:,14:16); % known as PL-PG
Prs=[0 0 0 ;Prs]; 
Qrs(:,1:3) = [ieee_Sub(:,8) ieee_Sub(:,10) ieee_Sub(:,12)];  % known as Ql
Qrs=[0 0 0 ;Qrs]; 
%% Optimization at Substation level

pT=[];cR(:,1:3)=Prs;cR(:,4:6)=Qrs;
%[V_S,Teta_S,Qg_S,Ptf,Qtf]=OPF_Sub(ieee_Sub,iter,Prs,Qrs);
[childTarget,parentResponse]=OPF_General(ieee_Sub,iter,pT,cR);

i=6; % To run the algorithm for the children of node 6
pT1=childTarget(i,:);
ieee_F=Net.SubNode(i).Feeder;
Prf(:,1:3) = [ieee_F(:,7) ieee_F(:,9) ieee_F(:,11)]-ieee_F(:,14:16); % known as Pl
Prf=[0 0 0 ;Prf]; 
Qrf(:,1:3) = [ieee_F(:,8) ieee_F(:,10) ieee_F(:,12)];  % known as Ql
Qrf=[0 0 0 ;Qrf];
cR1=[Prf,Qrf];

[childTarget,parentResponse]=OPF_General(ieee_F,iter,pT1,cR1);
JJ=1; % To run the algorithm for the the first child
pT2=childTarget(1,:);
cR2=[];
ieee_N=Net.SubNode(i).FeederNode(JJ).Neighborhood;
[childTarget,parentResponse]=OPF_General(ieee_N,iter,pT2,cR2);

