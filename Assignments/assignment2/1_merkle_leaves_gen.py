import argparse
import json
import random
from utils import calculate_hash
import math
import sys

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="generation of merkle tree leaves")
    parser.add_argument("--n_leaves", type=int, help="number of leaves to generate", default=100)
    parser.add_argument("--output_file", help="json file path to store json leaves", default="merkle_leaves.json")
    args = parser.parse_args()
    if args.n_leaves <= 0:
        print("number of leaves should be positive integer > 0")
        exit(-1)
    leaves = []
    i = random.randint(1, 10)
    padding_length = int(math.pow(2, math.ceil(math.log2(args.n_leaves))) - args.n_leaves)

    while len(leaves) != args.n_leaves:
        leaves.append(
            {
                "pos": len(leaves),
                "value": i,
                "hash": calculate_hash(i)
            }
        )
        i += random.randint(1, 5)
    for i in range(padding_length):
        leaves.append(
            {
                "pos": len(leaves),
                "value": sys.maxsize,
                "hash": calculate_hash(sys.maxsize)
            }
        )
    print(f"Successfully generate {len(leaves)} leaf (after padding)")
    with open(args.output_file, "w") as output_file_obj:
        json.dump(leaves, output_file_obj)



