% function f = LossObjectiveFunctionSub(x,Num_bus_Sub,Pflow,Qflow)
function f = Obj2(x,Num_bus_Sub,Pflow,Qflow,Pt,Qt,Pr,Qr,Phase,N_Neighbor)
deltaP_f2n=0;deltaQ_f2n=0;
for JJ=1:N_Neighbor+1
    deltaP_f2n=deltaP_f2n+(Pt(JJ)-Pr(JJ,Phase))^2;
    deltaQ_f2n=deltaQ_f2n+(Qt(JJ)-Qr(JJ,Phase))^2;                    
end

f= x(Num_bus_Sub+1)+...
    10000*((x(Num_bus_Sub+1)-Pflow)^2)+10000*((x(1)-Qflow)^2)+1000*deltaP_f2n+1000*deltaQ_f2n;
end


    