clc
clear all
close all
%% Initialization
Max_Feeder_Iter=0;Max_Neighbor_Iter=0;threshold_s2f=10;
Max_Feeder_Iteration=[];
Max_Neighbor_Iteration=[];
iter=1;

Bus_ref = 1;      % reference bus 
baseMVA = 2.500000;% MVA base
vb = 4.800;%kV
zb = (vb^2)/baseMVA;
Ib=vb/zb;
ft2mi=0.000189394;
Z=lineimpedance;  % Line impedance

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

% INPUTS
% ieee_Sub= Network configuration
% iter= number of iterations
% Prs= active power responses from feeder holons
% Qrs= active power responses from feeder holons

% OUTPUTS
% V_S= Voltages at all 38 nodes in substation holon (also used as target for children)
% Teta_S= Angles at all 38 nodes in substation holon (also used as target for children)
% Qg_S= Reactive power generation at all 38 nodes in substation holon
% Ptf= targets for children (active power) 
% Qtf= targets for children (reactive power)
[V_S,Teta_S,Qg_S,Ptf,Qtf]=OPF_Sub(ieee_Sub,iter,Prs,Qrs);

%save ('C:\Users\malekpour\Documents\Malekpour_Pahwa\Ahmad & Denise\Output_S.mat','V_S','Teta_S','Qg_S','Ptf','Qtf')        
%load ('C:\Users\malekpour\Documents\Malekpour_Pahwa\Ahmad & Denise\Output_S.mat');        

Num_bus=size(ieee_Sub,1)+1;
for i=1:Num_bus % for all feeder holons
    if isempty(Net.SubNode(i).Feeder)  
        
    elseif (i~=3)&&(i~=8)&&(i~=25)&&(i~=28)&&(i~=31)&&(i~=37)
        Prf=[];Qrf=[];
        ieee_F=Net.SubNode(i).Feeder;%The feeder holon is using this to build the network configuration at feeder level
        if (ieee_Sub(i-1,7)~=0)
            Phase=1;
        elseif (ieee_Sub(i-1,9)~=0) 
            Phase=2;
        elseif (ieee_Sub(i-1,11)~=0)
            Phase=3;
        end   
        N_Neighbor=size(ieee_F,1);
        Prf(:,1:3) = [ieee_F(:,7) ieee_F(:,9) ieee_F(:,11)]-ieee_F(:,14:16); % known as Pl
        Prf=[0 0 0 ;Prf]; 
        Qrf(:,1:3) = [ieee_F(:,8) ieee_F(:,10) ieee_F(:,12)];  % known as Ql
        Qrf=[0 0 0 ;Qrf];
        %% Optimization at Feeder level
        % INPUTS
        
        % ieee_F= Network configuration
        % Ptf= active power target from top
        % Qtf= reactive power target from top
        % Prf= active power responses from bottom holons
        % Qrf= reactive power responses from bottom holons
        % V_S= target voltage from top
        % Teta_S= target angle from top
        % Phase= Shows that the feeder is in Phase A (Phase=1), B(Phase=2) or C(Phase=3)
        
        % OUTPUTS
        % V_F= Voltages at all feeder nodes (also used as target for children)
        % Teta_F= Angles at all feeder nodes (also used as target for children)
        % Qg_F= Reactive power generation at all feeder nodes
        % Ptn= targets for children (active power) 
        % Qtn= targets for children (reactive power)        
        % Prs= responses for parent (active power)
        % Qrs= responses for parent (reactive power)
        [V_F,Teta_F,Qg_F,Ptn,Qtn,Prs,Qrs,deltaP_s2f,deltaQ_s2f, Max_Feeder_Iter]=...
        OPF_F(ieee_F,Ptf(i,Phase),Qtf(i,Phase),Prf,Qrf,V_S(i,Phase),Teta_S(i,Phase),Phase);
        %Prn=[];Qrn=[];
        for JJ=1:length(Net.SubNode(i).FeederNode)
            ieee_N=Net.SubNode(i).FeederNode(JJ).Neighborhood;%The Neighborhood holon is using this to build the network configuration at Neighborhood level
            %% Optimization at Neighborhood level
            % INPUTS

            % ieee_N= Network configuration
            % Ptn= active power target from top
            % Qtn= reactive power target from top
            % V_F= target voltage from top
            % Teta_F= target angle from top
            % Phase= Shows that the feeder is in Phase A (Phase=1), B(Phase=2) or C(Phase=3)

            % OUTPUTS
            % V_N= Voltages at all Neighborhood nodes 
            % Teta_N= Angles at all Neighborhood nodes 
            % Qg_N= Reactive power generation at homes
            % Prf= responses for parent (active power)
            % Qrf= responses for parent (reactive power)             
            [V_N,Teta_N,Qg_N,Prf,Qrf,deltaP_f2n,deltaQ_f2n, Max_Neighbor_Iter]=...
            OPF_N(ieee_N,Ptn(JJ),Qtn(JJ),V_F(JJ),Teta_F(JJ),Phase);

        
            %deltaP(JJ)=max(abs(Net.SubNode(i).FeederNode(JJ).Pup-Net.SubNode(i).FeederNode(JJ).Pdown));
            %deltaQ(JJ)=max(abs(Net.SubNode(i).FeederNode(JJ).Qup-Net.SubNode(i).FeederNode(JJ).Qdown)); 
        end
        %deltaP_f2n(i)=max(deltaP_f2n(i),max(deltaP));
        %deltaQ_f2n(i)=max(deltaQ_f2n(i),max(deltaQ));         
    else
        %[Net, Max_Feeder_Iter]=OPF_SinglePhase(Net,i,LABC,GABC);
    end
end



