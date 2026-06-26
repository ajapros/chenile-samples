type TagListProps = {
  title: string;
  values: string[];
};

export function TagList({ title, values }: TagListProps) {
  return (
    <div className="tag-group">
      <span>{title}</span>
      <div className="tag-list">
        {values.length === 0 ? <em className="empty-text">None</em> : null}
        {values.map((value) => (
          <strong className="tag" key={value}>
            {value}
          </strong>
        ))}
      </div>
    </div>
  );
}
