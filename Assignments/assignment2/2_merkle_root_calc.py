import argparse
import json
from utils import calculate_parent_hash
import math

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="calculation of merkle tree root")
    parser.add_argument("--merkle_leaves_file", help="merkle leaves file path", default="merkle_leaves.json")
    parser.add_argument("--output_file", help="file to store merkle tree root", default="merkle_root.json")
    args = parser.parse_args()
    with open(args.merkle_leaves_file, "r") as merkle_leaves_file_obj:
        leaves = json.load(merkle_leaves_file_obj)
    if len(leaves) != int(math.pow(2, math.floor(math.log2(len(leaves))))):
        print("number of leaves should be power of 2 and > 1")
        exit(-1)
    current_states = leaves
    while len(current_states) != 1:
        left, right = current_states.pop(0), current_states.pop(0)
        current_states.append({"hash": calculate_parent_hash(left["hash"], right["hash"])})
    print(f"Successfully Calculate Merkle Root: {current_states[0]['hash']}")
    with open(args.output_file, "w") as output_file_obj:
        json.dump(current_states[0]['hash'], output_file_obj)



