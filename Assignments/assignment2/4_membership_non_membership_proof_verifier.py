import argparse
import json
from utils import calculate_parent_hash, calculate_hash
import math



def verify_membership(merkle_root, proof_hashes, pos, value):
    """
    :param merkle_root: str (merkle tree root)
    :param proof_hashes: list[str] (membership proof)
    :param pos: int (position of element to prove its membership)
    :param value: int (value of element to prove its membership)
    :return: bool (True if membership is verified else False)
    """
    # your code here: use (merkle_root, proof_hashes, pos, value) to verify membership
    # you should use calculate_hash, calculate_parent_hash
    # return True if membership is verified else return False
    root_hash = calculate_hash(value)
    ######### YOUR CODE BEGINS HERE (Expected No. Lines: 7 lines) #########
    for i in range(len(proof_hashes)):
        if pos % 2 == 0:
            root_hash = calculate_parent_hash(root_hash, proof_hashes[i])
        else:
            root_hash = calculate_parent_hash(proof_hashes[i], root_hash)
        pos = pos // 2
    ###### YOUR CODE ENDS HERE #############
    return root_hash == merkle_root


def verify_non_membership(merkle_root, lower_bound_proof_hashes, upper_bound_proof_hashes,
                          lower_bound_pos, upper_bound_pos, lower_bound_value, upper_bound_value,
                          target_value):
    """
    :param merkle_root: str (merkle tree root)
    :param lower_bound_proof_hashes: list[str] (membership proof of lower bound element)
    :param upper_bound_proof_hashes: list[str] (membership proof of upper bound element)
    :param lower_bound_pos: int (position of lower bound element to prove its membership)
    :param upper_bound_pos: int (position of upper bound element to prove its membership)
    :param lower_bound_value: int (value of lower bound element to prove its membership)
    :param upper_bound_value: int (value of upper bound element to prove its membership)
    :param target_value: int (value of target value to prove its *non*-membership)
    :return: bool (True if membership is verified else False)
    """
    # your code here: use (merkle_root, lower_bound_proof_hashes, upper_bound_proof_hashes,
    # lower_bound_pos, upper_bound_pos, lower_bound_value, upper_bound_value, target_value) to verify non-membership
    # to verify non-membership:
    # 1) target value should be higher than lower bound value, less than upper bound value (handle corner cases)
    # 2) lower_bound_pos should be less than upper_bound_pos by one (handle corner cases)
    # 3) lower bound and upper bound membership should be verified (handle corner cases)
    # you should use verify_membership
    non_membership_verification = True
    ######### YOUR CODE BEGINS HERE (Expected No. Lines: 17 lines)#########
    lower_bound_verification = True
    upper_bound_verification = True
    if lower_bound_value != None and upper_bound_value != None:
        if target_value < lower_bound_value or target_value > upper_bound_value or lower_bound_pos + 1 != upper_bound_pos:
            return False
        lower_bound_verification = verify_membership(merkle_root, lower_bound_proof_hashes, lower_bound_pos, lower_bound_value)
        upper_bound_verification = verify_membership(merkle_root, upper_bound_proof_hashes, upper_bound_pos, upper_bound_value)
    elif upper_bound_value != None:
        if target_value > upper_bound_value or upper_bound_pos != 0:
            return False
        upper_bound_verification = verify_membership(merkle_root, upper_bound_proof_hashes, upper_bound_pos, upper_bound_value)
    elif lower_bound_value != None:
        if target_value < lower_bound_value and lower_bound_pos != pow(2, len(lower_bound_proof_hashes)) - 1:
            return False
        lower_bound_verification = verify_membership(merkle_root, lower_bound_proof_hashes, lower_bound_pos, lower_bound_value)
    non_membership_verification = lower_bound_verification and upper_bound_verification
    ######### YOUR CODE ENDS HERE #########
    return non_membership_verification


def verify(merkle_root, proof):
    if proof["type"] == "membership":
        proof_hashes = proof["hashes"]
        pos = proof["pos"]
        value = proof["value"]
        return verify_membership(merkle_root, proof_hashes, pos, value)
    else:
        lower_bound_proof_hashes = proof["lower_bound_hashes"]
        upper_bound_proof_hashes = proof["upper_bound_hashes"]
        lower_bound_pos = proof["lower_bound_pos"]
        upper_bound_pos = proof["upper_bound_pos"]
        lower_bound_value = proof["lower_bound_value"]
        upper_bound_value = proof["upper_bound_value"]
        target_value = proof["target_value"]
        return verify_non_membership(merkle_root, lower_bound_proof_hashes, upper_bound_proof_hashes, lower_bound_pos,
                                     upper_bound_pos, lower_bound_value, upper_bound_value, target_value)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="proof verifier")
    parser.add_argument("--merkle_root_file", help="path to merkle_tree_root file", default="merkle_root.json")
    parser.add_argument("--proof_file", help="path to proof file", default="proof.json")
    args = parser.parse_args()

    with open(args.merkle_root_file, "r") as merkle_root_file_obj:
        merkle_root = json.load(merkle_root_file_obj)
    with open(args.proof_file, "r") as proof_file_obj:
        proof = json.load(proof_file_obj)
    if verify(merkle_root, proof):
        print("Successfully verified!")
    else:
        print("Verification failed!")
