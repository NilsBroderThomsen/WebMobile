#!/usr/bin/env bash

API_URL="http://localhost:8080/api/users/2/entries"

if [ -z "$TOKEN" ]; then
  echo "TOKEN is not set. Run this first:"
  echo "export TOKEN=your_token"
  exit 1
fi

titles=(
  "Just Monday"
  "Productive day"
  "Chaos in my head"
  "Pretty okay"
  "Too little sleep"
  "Coffee to the rescue"
  "Everything is annoying"
  "In the flow"
  "Zero motivation"
  "Surprisingly good"
  "Brain fog deluxe"
  "Small win"
  "So-so"
  "Finally making progress"
  "After-work vibes"
)

contents=(
  "The start of the week was rough."
  "Got a lot done today."
  "Thoughts bouncing like pinballs."
  "Not good, not bad."
  "Sleeping more would have been nice."
  "Caffeine kept things from getting worse."
  "Everything was exhausting today."
  "Stayed focused for hours."
  "Nothing moved forward."
  "Better than expected."
  "Head feels like cotton."
  "Small sense of accomplishment."
  "Somehow made it through the day."
  "Slowly coming together."
  "Work done, brain off."
)

moods=(3 6 4 5 2 6 3 8 2 7 4 6 5 7 8)

for i in {0..14}; do
  echo "== Creating entry $((i+1)) =="

  curl -s -X POST "$API_URL" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d @- <<EOF
{
  "title": "${titles[$i]}",
  "content": "${contents[$i]}",
  "moodRating": ${moods[$i]}
}
EOF

  echo -e "\n----------------------\n"
  sleep 0.2
done

echo "15 entries created. The database is a little less empty now. 🎉"
