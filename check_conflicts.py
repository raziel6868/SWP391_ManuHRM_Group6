#!/usr/bin/env python3
import sys

with open('src/main/webapp/components/sidebar.jsp', encoding='utf-8') as f:
    lines = f.readlines()

print(f"Total lines: {len(lines)}")

# Find all conflict markers
in_conflict = False
for i, line in enumerate(lines, 1):
    s = line.strip()
    if s.startswith('<<<<') or s.startswith('>>>>') or s.startswith('===='):
        print(f"Line {i}: {s[:80]}")
        in_conflict = True

if not in_conflict:
    print("NO CONFLICTS FOUND")
