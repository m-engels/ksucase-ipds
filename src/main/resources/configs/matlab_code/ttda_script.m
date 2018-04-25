
% Two-Tier Double Auction MICAI

clc;clear all;
rand('seed',1);

%%%%%%%%%%%%%%%%%%%%%%%%%% Each Neighborhood k

N=4; % Number of Neighborhoods
NB=3; % Number of buyers at each Neighborhood, 3 for all neighborhoods for this test case.
NS=1; % Number of sellers at each Neighborhood, 1 for all neighborhoods for this test case.

for k=1:N
    demand(k,:)= 2+6*rand(1,NB); % demand matrix of each neighborhood. Rows show neighborhood k, column shows homes in neighborhood k.
    supply(k,:)= (NB/NS)*(2+6*rand(1,NS));% supply matrix of each neighborhood. Rows show neighborhood k, column shows homes in neighborhood k.
    bid(k,:)= (demand(k,:))./(10*min(demand(k,:))); % The factor in the denom. is to convert demands to cent values of around 10 cents.
    ask(k,:)= 0.11*max((NB/NS)*(supply(k,:)))./((NB/NS)*(supply(k,:))); % The numerator is to convert supplies to cent values of around 11. ( 1.1*10)
    % Neighborhood clearing price vector 1xN
    c0k(k,:)= (ask(k,:)+mean(bid(k,:)))/2; % Since the number of home agents in each neighborhood is limited to 4, just an adhoc calculation of the
    %%%market clearing price is sufficient.
    f(k,:)=-1*[bid(k,:)-c0k(k,:) c0k(k,:)-ask(k,:)];
    lb=zeros(1,NB+NS);
    ub=[demand(k,:) supply(k,:)];
    [x(k,:)]=linprog(f(k,:),[],[],[],[],lb,ub);
    if k==N
        x1=x';
        x1(1:NB,:)=-1*x1(1:NB,:);
        surdef=sum(x1);
        surdef=surdef';
    end   
end

demand1=demand';
Demand=sum(demand1); % Vector of Total demand of all neighborhoods.
Demand=Demand';
Supply=supply; % Vector of Total supply of all neighborhoods. Changes shall be made, like demand, when more than one seller exists in a neighborhood k.

%%%%%%%%%%%%%%%%%%%%%%%%%%% Each Grid level Feeder
bk= surdef./100;          % External bid of neighborhood k to other neighborhoods based on their suplus/deficit power.
c0=mean(c0k);
ff=-[c0k-c0]';
lbf=zeros(N,1);
ubf=abs(surdef);
Aeq=[]; 
beq=[]; 
[y]=linprog(ff,[],[],Aeq,beq,lbf,ubf);

