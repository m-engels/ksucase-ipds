%function [Voltage,Angle,Qinjection,Pt,Qt,deltaP_s2f,deltaQ_s2f]=OPF_Sub(ieee37,iter,Pr,Qr)
function [childTarget,parentResponse]=OPF_G(ieee,iter,pT,cR)
if isempty(pT)
    Prs=cR(:,1:3);Qrs=cR(:,4:6);
    [V_S,Teta_S,Qg_S,Ptf,Qtf]=OPF_Sub(ieee,iter,Prs,Qrs);
    childTarget=[V_S,Teta_S,Ptf,Qtf,Qg_S];
    parentResponse=[];
elseif isempty(cR)
    if pT(1,7)~=0
        Phase=1;
    elseif pT(1,8)~=0
        Phase=2;
    elseif pT(1,9)~=0
        Phase=3;
    end
    Pt=pT(7:9);
    Ptn=Pt(Phase);
    Qt=pT(10:12);
    Qtn=Qt(Phase);
    V_FF=pT(1:3);
    V_F=V_FF(Phase);
    Teta_FF=pT(4:6);
    Teta_F=Teta_FF(Phase);
    [V_N,Teta_N,Qg_N,Prf,Qrf,deltaP_f2n,deltaQ_f2n, Max_Neighbor_Iter]=...
    OPF_N(ieee,Ptn,Qtn,V_F,Teta_F,Phase);    
    V_NN=zeros(size(V_N,1),3);V_NN(:,Phase)=V_N;
    Teta_NN=zeros(size(Teta_N,1),3);Teta_NN(:,Phase)=Teta_N;
    Qg_NN=zeros(size(Qg_N,1),3);Qg_NN(:,Phase)=Qg_N;
    PPth=zeros(size(Qg_NN));
    QQth=zeros(size(Qg_NN));     
    childTarget=[V_NN(2:end,:),Teta_NN(2:end,:),PPth,QQth,Qg_NN];
    PPrf=zeros(1,3);PPrf(Phase)=Prf;
    QQrf=zeros(1,3);QQrf(Phase)=Qrf;
    parentResponse=[PPrf,QQrf];
else
    if pT(1,7)~=0
        Phase=1;
    elseif pT(1,8)~=0
        Phase=2;
    elseif pT(1,9)~=0
        Phase=3;
    end
    Pt=pT(7:9);
    Ptf=Pt(Phase);
    Qt=pT(10:12);
    Qtf=Qt(Phase);
    V_SS=pT(1:3);
    V_S=V_SS(Phase);
    Teta_SS=pT(4:6);
    Teta_S=Teta_SS(Phase);
    Prf=cR(:,1:3);
    Qrf=cR(:,4:6);
    [V_F,Teta_F,Qg_F,Ptn,Qtn,Prs,Qrs,deltaP_s2f,deltaQ_s2f,Max_Feeder_Iter]=...
    OPF_F(ieee,Ptf,Qtf,Prf,Qrf,V_S,Teta_S,Phase);
    V_FF=zeros(size(V_F,1),3);V_FF(:,Phase)=V_F;
    Teta_FF=zeros(size(Teta_F,1),3);Teta_FF(:,Phase)=Teta_F;
    PPtn=zeros(size(Ptn,1),3);PPtn(:,Phase)=Ptn;
    QQtn=zeros(size(Qtn,1),3);QQtn(:,Phase)=Qtn;
    Qg_FF=zeros(size(Qg_F,1),3);Qg_FF(:,Phase)=Qg_F;
    childTarget=[V_FF(2:end,:),Teta_FF(2:end,:),PPtn,QQtn,Qg_FF];
    PPrs=zeros(1,3);PPrs(Phase)=Prs;
    QQrs=zeros(1,3);QQrs(Phase)=Qrs;
    parentResponse=[PPrs,QQrs];
end

