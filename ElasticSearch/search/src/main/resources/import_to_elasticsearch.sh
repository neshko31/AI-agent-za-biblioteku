#!/bin/bash
ES_HOST="${1:-http://localhost:9200}"

echo "==> Creating index: persons"
curl -s -X PUT "$ES_HOST/persons" -H 'Content-Type: application/json' -d @persons_mapping.json | python3 -m json.tool

echo ""
echo "==> Creating index: books"
curl -s -X PUT "$ES_HOST/books" -H 'Content-Type: application/json' -d @books_mapping.json | python3 -m json.tool

echo ""
echo "==> Bulk importing persons (1000 records)..."
curl -s -X POST "$ES_HOST/_bulk" -H 'Content-Type: application/x-ndjson' --data-binary @persons_bulk.ndjson | python3 -c "
import sys,json; d=json.load(sys.stdin)
errs=[i for i in d.get('items',[]) if list(i.values())[0].get('status',200)>=400]
print(f'persons: {len(d[chr(34)]items[chr(34)])} items, {len(errs)} errors')
"

echo ""
echo "==> Bulk importing books (1000 records)..."
curl -s -X POST "$ES_HOST/_bulk" -H 'Content-Type: application/x-ndjson' --data-binary @books_bulk.ndjson | python3 -c "
import sys,json; d=json.load(sys.stdin)
errs=[i for i in d.get('items',[]) if list(i.values())[0].get('status',200)>=400]
print(f'books: {len(d[chr(34)]items[chr(34)])} items, {len(errs)} errors')
"

echo ""
echo "==> Counts:"
curl -s "$ES_HOST/persons/_count" | python3 -m json.tool
curl -s "$ES_HOST/books/_count"   | python3 -m json.tool
