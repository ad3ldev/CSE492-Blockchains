import argparse
import json
from utils import calculate_parent_hash, is_sorted, get_values_and_hashes
import math

def get_parent(pos):
    return (pos - 1) // 2

def create_hash_tree(leaf_hashes):
    size = 2* len(leaf_hashes) - 1
    tree = [None for i in range(size)]
    start = len(leaf_hashes) - 1
    end = size
    for i in range(len(leaf_hashes)):
        tree[i + start] = leaf_hashes[i]
    while start != 0:
        for i in range(start, end, 2):
            parent = (i - 1) // 2
            left = tree[i]
            right = tree[i + 1]
            tree[parent] = calculate_parent_hash(left, right)
        end = start
        start = start // 2
    return tree

def find_lower_bound(value_to_prove, leaf_values):
    pos = - 1
    size = len(leaf_values)
    found = False
    while not found:
        if value_to_prove in leaf_values:
            found = True
            pos = leaf_values.index(value_to_prove)
            for i in range(pos, size):
                if leaf_values[i] != value_to_prove:
                    pos = i - 1
                    return pos
        else:
            value_to_prove -= 1
    return pos
def find_upper_bound(value_to_prove, leaf_values):
    pos = - 1
    size = len(leaf_values)
    found = False
    while not found:
        if value_to_prove in leaf_values:
            found = True
            pos = leaf_values.index(value_to_prove)
            for i in range(pos, size):
                if leaf_values[i] != value_to_prove:
                    pos = i - 1
                    return pos
        else:
            value_to_prove += 1
    return pos


def gen_non_membership_proof(value_to_prove, leaf_values, leaf_hashes):
    """
    :param value_to_prove: int (value to prove its non-membership)
    :param leaf_values: list[int] (list of merkle tree leaf values)
    :param leaf_hashes: list[str] (list of merkle tree leaf hashes)
    :return: list[str] (membership proof of lower bound element),
             list[str] (membership proof of upper bound element),
             int (position of lower bound element),
             int (position of upper bound element)
    """
    non_membership_proof_lower_bound = []
    non_membership_proof_higher_bound = []
    lower_bound_pos = -1
    upper_bound_pos = -1
    # your code here: use (value_to_prove, leaf_values, leaf_hashes) to generate non-membership proof of element with value = `value_to_proof`
    # you should get lower and higher bounds positions
    # (if a bound doesn't exist in case of value_to_proof < min(leaf_values)(only upper exists here) or value_to_proof > max(leaf_values)(only lower exists here), let it -1)
    # calculate membership proof for each bound
    # (if bound doesn't exist in case of value_to_proof < min(leaf_values) or value_to_proof > max(leaf_values), let the proof be empty list)
    # you will use `gen_membership_proof`
    ######### YOUR CODE BEGINS HERE (Expected No. Lines: 12 lines) #########
    lower_bound_pos = find_lower_bound(value_to_prove, leaf_values)
    upper_bound_pos = find_upper_bound(value_to_prove, leaf_values)
    non_membership_proof_lower_bound = gen_membership_proof(lower_bound_pos, leaf_hashes)
    non_membership_proof_higher_bound = gen_membership_proof(upper_bound_pos, leaf_hashes)
    ###### YOUR CODE ENDS HERE #############
    return non_membership_proof_lower_bound, non_membership_proof_higher_bound, lower_bound_pos, upper_bound_pos


def gen_membership_proof(pos, leaf_hashes):
    """
    :param pos: int (position of leaf to get membership proof for)
    :param leaf_hashes: list[str] (list of merkle tree leaf hashes)
    :return: list[str] (membership proof for element as position `pos`)
    """
    leaf_hashes = leaf_hashes.copy()
    membership_proof = []
    # your code here: use (pos, leaf_hashes) to generate membership proof of element at position `pos`
    # to get next level hash use `calculate_parent_hash`
    ######### YOUR CODE BEGINS HERE (Expected No. Lines: 12 lines)  #########
    tree_hashes = create_hash_tree(leaf_hashes)
    pos = len(tree_hashes) - len(leaf_hashes) + pos
    while pos != 0:
        parent = get_parent(pos)
        sibling = pos - 1 if pos % 2 == 0 else pos + 1
        membership_proof.append(tree_hashes[sibling])
        pos = parent
    ###### YOUR CODE ENDS HERE #############
    return membership_proof


def gen_proof(value_to_prove, leaf_values, leaf_hashes):
    """
    :param value_to_prove: int (value to proof its membership / non-membership)
    :param leaf_values: list[int] (list of merkle tree leaf values)
    :param leaf_hashes: list[str] (list of merkle tree leaf hashes)
    :return: bool (indicates membership 'true' / non-membership 'false'),
             object: proof of membership/non-membership
    """
    if value_to_prove in leaf_values:
        pos = leaf_values.index(value_to_prove)
        membership_proof = gen_membership_proof(pos, leaf_hashes)
        return True, (membership_proof, pos)
    else:
        non_membership_proof = gen_non_membership_proof(
            value_to_prove, leaf_values, leaf_hashes)
        return False, non_membership_proof


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="generation of membership / non membership proof")
    parser.add_argument("--value", type=int,
                        help="value to check its membership or non membership")
    parser.add_argument("--merkle_leaves_file",
                        help="file to merkle leaves", default="merkle_leaves.json")
    parser.add_argument(
        "--proof_file", help="file to save proof", default="proof.json")
    args = parser.parse_args()
    with open(args.merkle_leaves_file, "r") as merkle_leaves_file_obj:
        leaves = json.load(merkle_leaves_file_obj)

    if len(leaves) != int(math.pow(2, math.floor(math.log2(len(leaves))))) and is_sorted(leaves):
        print("number of leaves should be power of 2 and > 1 and sorted according to leaf_values")
        exit(-1)
    leaf_values, leaf_hashes = get_values_and_hashes(leaves)
    membership_flag, proof = gen_proof(args.value, leaf_values, leaf_hashes)
    proof_result = {}
    if membership_flag:
        membership_proof, pos = proof
        proof_result["type"] = "membership"
        proof_result["hashes"] = membership_proof
        proof_result["pos"] = pos
        proof_result["value"] = args.value
    else:
        non_membership_proof_lower_bound, non_membership_proof_upper_bound, lower_bound_pos, upper_bound_pos = proof
        proof_result["type"] = "non-membership"
        proof_result["lower_bound_hashes"] = non_membership_proof_lower_bound
        proof_result["upper_bound_hashes"] = non_membership_proof_upper_bound
        proof_result["lower_bound_pos"] = lower_bound_pos
        proof_result["upper_bound_pos"] = upper_bound_pos
        proof_result["lower_bound_value"] = None if lower_bound_pos == - \
            1 else leaf_values[lower_bound_pos]
        proof_result["upper_bound_value"] = None if upper_bound_pos == - \
            1 else leaf_values[upper_bound_pos]
        proof_result["target_value"] = args.value
    with open(args.proof_file, "w") as proof_file_obj:
        json.dump(proof_result, proof_file_obj)
