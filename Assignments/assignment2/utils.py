from hashlib import sha256

def calculate_hash(value):
    hash_object = sha256()
    hash_object.update(str(value).encode())
    hash_hex = hash_object.hexdigest()
    return hash_hex

def calculate_parent_hash(left_hash, right_hash):
    hash_object = sha256()
    hash_object.update(left_hash.encode())
    hash_object.update(right_hash.encode())
    hash_hex = hash_object.hexdigest()
    return hash_hex

def get_values_and_hashes(merkle_leaves):
    values, hashes = [], []
    for merkle_leaf in merkle_leaves:
        values.append(merkle_leaf["value"])
        hashes.append(merkle_leaf["hash"])
    return values, hashes

def is_sorted(merkle_leaves):
    return all(merkle_leaves[i]["value"] <= merkle_leaves[i + 1]["value"] for i in range(len(merkle_leaves) - 1))

