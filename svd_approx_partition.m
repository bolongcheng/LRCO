function[f,g,shift] = svd_approx_partition(sample_VF,ranks_rows,ranks_cols,t)
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% This implements the L-1 penality regression of eqn (26), cplex is
% required for solving the LP and a parallel for loop is used. Enable
% parpool in sample_states.m
%
%sample_VF are the value functions computed for the sampled states
%ranks_rows is the number of submatrices from top to bottom
%ranks_cols is the number of submatrices from left to right
%t is the time step
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

diary('diary_cp.txt');
% diary on;
global A_part_glob sample_states_part_glob I_glob; %
% options = optimset('Algorithm', 'trust-region-reflective', 'GradObj', 'on', 'Hessian', 'on', 'MaxPCGIter', 100, 'Display', 'iter', 'MaxIter', 2);%, 'Display', 'iter');
optionsLP = cplexoptimset('cplex');
optionsLP.Algorithm = 'dual';
optionsLP.LargeScale = 'on';

A_part = A_part_glob; %binary matrix where 1 means the entry is sampled,0 otherwise. (A_part is the binary matrix for the submatrices)
sample_states_part = sample_states_part_glob; %indices of sampled states of the submatrices
I = I_glob;
%size of submatrices
[size_row, size_col] = size(A_part);
num_elements_part = size_row+size_col; %number of decision variables
sample_size = size(sample_states_part,1);


%Linear program formulation: min cx subject to Ax<=b
fun = vertcat(zeros(num_elements_part,1), ones(sample_size,1)); %the c vector

% sample_states_i = sample_states + ones(size(sample_states));
shift=zeros(ranks_rows,ranks_cols); %the shift at each submatrix (to avoid taking the log of a negative number)

f = zeros(size_row,ranks_rows,ranks_cols); %the x vector, for each submatrix
g = zeros(size_col,ranks_rows,ranks_cols); %the y vector, for each submatrix

%loop over each submatrix, and perform low rank approximation, find x,y and shift.
%rank1 and rank2 are the indices of the submatrix.
s_mat = reshape((0:size_row*size_col-1)*sample_size,ranks_rows,[]);

indi = sub2ind(size(A_part),sample_states_part(:,1),sample_states_part(:,2));
parfor rank2 = 1:ranks_cols
    shift_temp = zeros(ranks_rows,1);
    for rank1 = 1:ranks_rows
        sample_shift = s_mat(rank1,rank2);
        V_temp = zeros(size_row, size_col);
        V_temp(indi) = sample_VF(sample_shift+1:sample_shift + sample_size);
        V_sample = V_temp;
        min_V_sample = min(V_sample(:));
        shift_temp(rank1) = min(-min_V_sample + 1,0);
        V_sample = V_sample + shift_temp(rank1)*A_part;
        
        b = log(V_sample(indi))./log(1.0001);
        b = vertcat(b,-b);
        
        a_b = cplexlp(fun, I, b,[],[],[],[],[],optionsLP);        
        %contains both x and y concatenated        
        f_curr_part = 1.0001.^(a_b(1 : size_row)); %x vector for submatrix
        g_curr_part = 1.0001.^(a_b(size_row+1 : num_elements_part));%y vector for submatrix
   
        
        f(:,rank1,rank2) = f_curr_part;
        g(:,rank1,rank2) = g_curr_part;
    end
    shift(:,rank2) = shift_temp;
end
diary off;
end
