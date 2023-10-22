Stage 1 (generation of leaves):
    
    python 1_merkle_leaves_gen.py --n_leaves 100
Stage 2 (computation of merkle tree root):
    
    python 2_merkle_root_calc.py --merkle_leaves_file merkle_leaves.json

Stage 3 (calculate proof of membership/non-membership to a given value)

    python 3_membership_non_membership_proof_gen.py --merkle_leaves_file merkle_leaves.json --value 10 --proof_file 10.json

Stage 4 (verify proof of membership/non-membership)

    python 4_membership_non_membership_proof_verifier.py --merkle_root_file merkle_root.json --proof_file 10.json