function [childTarget,parentResponse]=OPF_General(ieee,iter,pT,cR)
childTarget=[];parentResponse=[];
Phase_A=[];Phase_B=[];Phase_C=[];
% childTarget_B=[];parentResponse_B=[];
% childTarget_C=[];parentResponse_C=[];
if isempty(pT)
    Prs=cR(:,1:3);Qrs=cR(:,4:6);
    [V_S,Teta_S,Qg_S,Ptf,Qtf]=OPF_Sub(ieee,iter,Prs,Qrs);
    childTarget=[V_S,Teta_S,Ptf,Qtf,Qg_S];
    parentResponse=[];
elseif isempty(cR)
    if pT(1,7)~=0
        Phase_A=1;
    end
    if pT(1,8)~=0
        Phase_B=2;
    end
    if pT(1,9)~=0
        Phase_C=3;
    end
    if Phase_A==1
        Pt=pT(7:9);
        Ptn=Pt(Phase_A);
        Qt=pT(10:12);
        Qtn=Qt(Phase_A);
        V_FF=pT(1:3);
        V_F=V_FF(Phase_A);
        Teta_FF=pT(4:6);
        Teta_F=Teta_FF(Phase_A);
        [V_N,Teta_N,Qg_N,Prf,Qrf,deltaP_f2n,deltaQ_f2n, Max_Neighbor_Iter]=...
        OPF_N(ieee,Ptn,Qtn,V_F,Teta_F,Phase_A);    
        V_NN=zeros(size(V_N,1),3);V_NN(:,Phase_A)=V_N;
        Teta_NN=zeros(size(Teta_N,1),3);Teta_NN(:,Phase_A)=Teta_N;
        Qg_NN=zeros(size(Qg_N,1),3);Qg_NN(:,Phase_A)=Qg_N;
        PPth=zeros(size(Qg_NN));
        QQth=zeros(size(Qg_NN));     
        childTarget_A=[V_NN(2:end,:),Teta_NN(2:end,:),PPth,QQth,Qg_NN];
        PPrf=zeros(1,3);PPrf(Phase_A)=Prf;
        QQrf=zeros(1,3);QQrf(Phase_A)=Qrf;
        parentResponse_A=[PPrf,QQrf];
        if isempty(parentResponse)
            %childTarget=zeros(size(childTarget_A));
            parentResponse=zeros(size(parentResponse_A));
        end
        %childTarget=childTarget+childTarget_A;
        parentResponse=parentResponse+parentResponse_A; 
    end
    if Phase_B==2
        Pt=pT(7:9);
        Ptn=Pt(Phase_B);
        Qt=pT(10:12);
        Qtn=Qt(Phase_B);
        V_FF=pT(1:3);
        V_F=V_FF(Phase_B);
        Teta_FF=pT(4:6);
        Teta_F=Teta_FF(Phase_B);
        [V_N,Teta_N,Qg_N,Prf,Qrf,deltaP_f2n,deltaQ_f2n, Max_Neighbor_Iter]=...
        OPF_N(ieee,Ptn,Qtn,V_F,Teta_F,Phase_B);    
        V_NN=zeros(size(V_N,1),3);V_NN(:,Phase_B)=V_N;
        Teta_NN=zeros(size(Teta_N,1),3);Teta_NN(:,Phase_B)=Teta_N;
        Qg_NN=zeros(size(Qg_N,1),3);Qg_NN(:,Phase_B)=Qg_N;
        PPth=zeros(size(Qg_NN));
        QQth=zeros(size(Qg_NN));     
        childTarget_B=[V_NN(2:end,:),Teta_NN(2:end,:),PPth,QQth,Qg_NN];
        PPrf=zeros(1,3);PPrf(Phase_B)=Prf;
        QQrf=zeros(1,3);QQrf(Phase_B)=Qrf;
        parentResponse_B=[PPrf,QQrf];
        if isempty(parentResponse)
            %childTarget=zeros(size(childTarget_B));
            parentResponse=zeros(size(parentResponse_B));
        end
        %childTarget=childTarget+childTarget_B;
        parentResponse=parentResponse+parentResponse_B;         
    end  
    if Phase_C==3
        Pt=pT(7:9);
        Ptn=Pt(Phase_C);
        Qt=pT(10:12);
        Qtn=Qt(Phase_C);
        V_FF=pT(1:3);
        V_F=V_FF(Phase_C);
        Teta_FF=pT(4:6);
        Teta_F=Teta_FF(Phase_C);
        [V_N,Teta_N,Qg_N,Prf,Qrf,deltaP_f2n,deltaQ_f2n, Max_Neighbor_Iter]=...
        OPF_N(ieee,Ptn,Qtn,V_F,Teta_F,Phase_C);    
        V_NN=zeros(size(V_N,1),3);V_NN(:,Phase_C)=V_N;
        Teta_NN=zeros(size(Teta_N,1),3);Teta_NN(:,Phase_C)=Teta_N;
        Qg_NN=zeros(size(Qg_N,1),3);Qg_NN(:,Phase_C)=Qg_N;
        PPth=zeros(size(Qg_NN));
        QQth=zeros(size(Qg_NN));     
        [V_NN(2:end,:),Teta_NN(2:end,:),PPth,QQth,Qg_NN];
        PPrf=zeros(1,3);PPrf(Phase_C)=Prf;
        QQrf=zeros(1,3);QQrf(Phase_C)=Qrf;
        parentResponse_C=[PPrf,QQrf];
        if isempty(parentResponse)
            %childTarget=zeros(size(childTarget_C));
            parentResponse=zeros(size(parentResponse_C));
        end
        %childTarget=childTarget+childTarget_C;
        parentResponse=parentResponse+parentResponse_C;         
    end    
%     childTarget=sum(childTarget);
%     parentResponse=sum(parentResponse);
else
    if pT(1,7)~=0
        Phase_A=1;
    end
    if pT(1,8)~=0
        Phase_B=2;
    end
    if pT(1,9)~=0
        Phase_C=3;
    end
    if Phase_A==1
        Pt=pT(7:9);
        Ptf=Pt(Phase_A);
        Qt=pT(10:12);
        Qtf=Qt(Phase_A);
        V_SS=pT(1:3);
        V_S=V_SS(Phase_A);
        Teta_SS=pT(4:6);
        Teta_S=Teta_SS(Phase_A);
        Prf=cR(:,1:3);
        Qrf=cR(:,4:6);
        [V_F,Teta_F,Qg_F,Ptn,Qtn,Prs,Qrs,deltaP_s2f,deltaQ_s2f,Max_Feeder_Iter]=...
        OPF_F(ieee,Ptf,Qtf,Prf,Qrf,V_S,Teta_S,Phase_A);
        V_FF=zeros(size(V_F,1),3);V_FF(:,Phase_A)=V_F;
        Teta_FF=zeros(size(Teta_F,1),3);Teta_FF(:,Phase_A)=Teta_F;
        PPtn=zeros(size(Ptn,1),3);PPtn(:,Phase_A)=Ptn;
        QQtn=zeros(size(Qtn,1),3);QQtn(:,Phase_A)=Qtn;
        Qg_FF=zeros(size(Qg_F,1),3);Qg_FF(:,Phase_A)=Qg_F;
        childTarget_A=[V_FF(2:end,:),Teta_FF(2:end,:),PPtn,QQtn,Qg_FF];
        PPrs=zeros(1,3);PPrs(Phase_A)=Prs;
        QQrs=zeros(1,3);QQrs(Phase_A)=Qrs;
        parentResponse_A=[PPrs,QQrs];
        if isempty(childTarget)
            childTarget=zeros(size(childTarget_A));
            parentResponse=zeros(size(parentResponse_A));
        end
        childTarget=childTarget+childTarget_A;
        parentResponse=parentResponse+parentResponse_A;       
    end
    if Phase_B==2
        Pt=pT(7:9);
        Ptf=Pt(Phase_B);
        Qt=pT(10:12);
        Qtf=Qt(Phase_B);
        V_SS=pT(1:3);
        V_S=V_SS(Phase_B);
        Teta_SS=pT(4:6);
        Teta_S=Teta_SS(Phase_B);
        Prf=cR(:,1:3);
        Qrf=cR(:,4:6);
        [V_F,Teta_F,Qg_F,Ptn,Qtn,Prs,Qrs,deltaP_s2f,deltaQ_s2f,Max_Feeder_Iter]=...
        OPF_F(ieee,Ptf,Qtf,Prf,Qrf,V_S,Teta_S,Phase_B);
        V_FF=zeros(size(V_F,1),3);V_FF(:,Phase_B)=V_F;
        Teta_FF=zeros(size(Teta_F,1),3);Teta_FF(:,Phase_B)=Teta_F;
        PPtn=zeros(size(Ptn,1),3);PPtn(:,Phase_B)=Ptn;
        QQtn=zeros(size(Qtn,1),3);QQtn(:,Phase_B)=Qtn;
        Qg_FF=zeros(size(Qg_F,1),3);Qg_FF(:,Phase_B)=Qg_F;
        childTarget_B=[V_FF(2:end,:),Teta_FF(2:end,:),PPtn,QQtn,Qg_FF];
        PPrs=zeros(1,3);PPrs(Phase_B)=Prs;
        QQrs=zeros(1,3);QQrs(Phase_B)=Qrs;
        parentResponse_B=[PPrs,QQrs];
        if isempty(childTarget)
            childTarget=zeros(size(childTarget_B));
            parentResponse=zeros(size(parentResponse_B));
        end
        childTarget=childTarget+childTarget_B;
        parentResponse=parentResponse+parentResponse_B; 
    end
    if Phase_C==3
        Pt=pT(7:9);
        Ptf=Pt(Phase_C);
        Qt=pT(10:12);
        Qtf=Qt(Phase_C);
        V_SS=pT(1:3);
        V_S=V_SS(Phase_C);
        Teta_SS=pT(4:6);
        Teta_S=Teta_SS(Phase_C);
        Prf=cR(:,1:3);
        Qrf=cR(:,4:6);
        [V_F,Teta_F,Qg_F,Ptn,Qtn,Prs,Qrs,deltaP_s2f,deltaQ_s2f,Max_Feeder_Iter]=...
        OPF_F(ieee,Ptf,Qtf,Prf,Qrf,V_S,Teta_S,Phase_C);
        V_FF=zeros(size(V_F,1),3);V_FF(:,Phase_C)=V_F;
        Teta_FF=zeros(size(Teta_F,1),3);Teta_FF(:,Phase_C)=Teta_F;
        PPtn=zeros(size(Ptn,1),3);PPtn(:,Phase_C)=Ptn;
        QQtn=zeros(size(Qtn,1),3);QQtn(:,Phase_C)=Qtn;
        Qg_FF=zeros(size(Qg_F,1),3);Qg_FF(:,Phase_C)=Qg_F;
        childTarget_C=[V_FF(2:end,:),Teta_FF(2:end,:),PPtn,QQtn,Qg_FF];
        PPrs=zeros(1,3);PPrs(Phase_C)=Prs;
        QQrs=zeros(1,3);QQrs(Phase_C)=Qrs;
        parentResponse_C=[PPrs,QQrs];
        if isempty(childTarget)
            childTarget=zeros(size(childTarget_C));
            parentResponse=zeros(size(parentResponse_C));
        end
        childTarget=childTarget+childTarget_C;
        parentResponse=parentResponse+parentResponse_C;        
    end    
%     childTarget=sum(childTarget);
%     parentResponse=sum(parentResponse);        
end

