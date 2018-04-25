function [result]=ttda2(A)
% Two-Tier Double Auction MICAI
%%%%%%%%%%%%%%%%%%%%%%%%%% Each Neighborhood k
% [A,I]=sortrows(A,5);
N=4; % Number of Neighborhoods
% NB=length(A(find(A(:,4)==1),4)); % Number of buyers at each Neighborhood, 3 for all neighborhoods for this test case.
% NS=length(A(find(A(:,4)==0),4)); % Number of sellers at each Neighborhood, 1 for all neighborhoods for this test case.
NB=2;
NS=2;
% B=zeros(NB+NS,5);
% a=find(A(:,4)==1);
% b=find(A(:,4)==0);
% B(NB+1:NB+NS,:)=A(a,:);
% B(1:NB,:)=A(b,:);
demand = A(1:NB,5); % demand matrix of each neighborhood. Rows show neighborhood k, column shows homes in neighborhood k.
supply = A(NB+1:NB+NS,5);
buy= A(1:NB,2); % The factor in the denom. is to convert demands to cent values of around 10 cents.
sell= A(NB+1:NB+NS,3);
% Neighborhood clearing price vector 1xN
c0= (min(sell)+max(buy))/2; % Since the number of home agents in each neighborhood is limited to 4, just an adhoc calculation of the
%%%market clearing price is sufficient.
f=-1*[(buy-c0)' (c0-sell)'];
lb=zeros(NB+NS,1);
ub=[demand; supply];
Aeq=[-1 -1 1 1];
beq=0;
x=linprog(f,[],[],Aeq,beq,lb,ub);
c0kvector=repmat(c0,4,1);
% result=zeros(NB+NS,1);
% a1=find(A(:,4)==1);
% b1=find(A(:,4)==0);
% result(a)=x(a1);
% result(b)=x(b1);
% r=result;
result=[x c0kvector];



% def=a.*r;
% def=sum(def);
% surdef=max(r)-def;
end