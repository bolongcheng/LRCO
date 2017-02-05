function[f,g,shift] = svd_approx_partitionLS(sample_VF,ranks_rows,ranks_cols,t)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% This implements the L-1 penality regression of eqn (26)
%
%sample_VF are the value functions computed for the sampled states
%ranks_rows is the number of submatrices from top to bottom
%ranks_cols is the number of submatrices from left to right
%t is the time step
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

diary('diary_l2.txt');
% diary on;
global A_part_glob sample_states_part_glob LeastSq_glob;

A_part = A_part_glob; %binary matrix where 1 means the entry is sampled,0 otherwise. (A_part is the binary matrix for the submatrices)
sample_states_part = sample_states_part_glob; %indices of sampled states of the submatrices
LeastSq = LeastSq_glob;
%size of submatrices
[size_row, size_col] = size(A_part);
num_elements_part = size_row+size_col; %number of decision variables
sample_size = size(sample_states_part,1); % number of samples per submatrix

shift=zeros(ranks_rows,ranks_cols); %the shift at each submatrix (to avoid taking the log of a negative number)
f = zeros(size_row,ranks_rows,ranks_cols); %the x vector, for each submatrix
g = zeros(size_col,ranks_rows,ranks_cols); %the y vector, for each submatrix

sample_VF = reshape(sample_VF,sample_size,[]); %reshape to sample_size x num_of_sub
shift = min(-min(sample_VF(:))+1,0) + shift; 
sample_VF = sample_VF + shift(1,1); %use the same shift value for all submatrices
b = log(sample_VF)./log(1.0001); 
a_b = LeastSq * b;  % a_b is the predicted values for all submatrices


f_part = 1.0001.^(a_b(1 : size_row,:)); %x vector for submatrix
g_part = 1.0001.^(a_b(size_row+1 : num_elements_part,:));%y vector for submatrix

k = 1;
for rank2 = 1:ranks_cols
    for rank1 = 1:ranks_rows
        f(:,rank1,rank2) =  f_part(:,k); 
        g(:,rank1,rank2) = g_part(:,k); 
        k=k+1;
    end
end

diary off;
end
