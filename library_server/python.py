import csv
import re

# Mapping for known series prefixes
series_prefixes = [
    ("The Famous Five", "the_famous_five_"),
    ("The Secret Seven", "the_secret_seven_"),
    ("Diary Of A Wimpy Kid", "diary_of_a_wimpy_kid_"),
    ("Harry Potter", "harry_potter_"),
    ("Percy Jackson", "percy_jackson_"),
]

def title_to_filename(title):
    original_title = title
    filename = title.lower()
    # Remove series prefix for filename if present, will add it back in a moment
    prefix = ""
    for series, prefix_candidate in series_prefixes:
        if filename.startswith(series.lower()):
            prefix = prefix_candidate
            filename = filename[len(series):].strip(": -")
            break
    # Replace non-alphanumeric with underscores
    filename = re.sub(r"[^a-z0-9]+", "_", filename)
    filename = filename.strip("_")
    # Add prefix back
    filename = prefix + filename
    # Remove double underscores
    filename = re.sub(r"_+", "_", filename)
    # Add .jpg extension
    filename += ".jpg"
    return filename

with open('books.csv', newline='', encoding='utf-8') as csvfile:
    reader = csv.DictReader(csvfile)
    for row in reader:
        book_id = row['id']
        title = row['title']
        image_filename = title_to_filename(title)
        print(f"UPDATE books SET image = '{image_filename}' WHERE id = {book_id};")