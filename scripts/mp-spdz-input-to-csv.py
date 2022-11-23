#!/usr/bin/python3

'''
This file is a part of the Sharemind framework.
Copyright (C) Cybernetica AS

All rights are reserved. Reproduction in whole or part is prohibited
without the written consent of the copyright owner. The usage of this
code is subject to the appropriate license agreement.

----

This takes the generated input from the `research-prototype` (i.e.
MP-SPDZ) implementation of IPA and turns it into a valid CSV for
`sharemind-csv-importer`.
The MP-SPDZ inputs don't have a header, which the csv importer
requires.

Running:
    `./mp-spdz-input-to-csv.py <input file>`
'''


def main(filename: str):
    out_filename = f"{filename}.csv"
    with open(out_filename, "w") as of:
        of.write("match_key is_trigger value breakdown_key\n")  # Using space as delimiter
        with open(filename, "r") as f:
            for line in f:
                line = line.strip().split()
                line[-1] = str(int(line[-1]) + 1)
                of.write(" ".join(line) + "\n")


if __name__ == "__main__":
    import sys
    main(sys.argv[1])
