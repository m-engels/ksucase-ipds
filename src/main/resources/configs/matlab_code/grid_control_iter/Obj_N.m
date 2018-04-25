% function f = LossObjectiveFunctionSub(x,Num_bus_Sub,Pflow,Qflow)
function f = Obj_N(x,Num_bus_Sub,Pflow,Qflow)
f= x(Num_bus_Sub+1)+...
    10000*((x(Num_bus_Sub+1)-Pflow)^2)+10000*((x(1)-Qflow)^2);
end


    