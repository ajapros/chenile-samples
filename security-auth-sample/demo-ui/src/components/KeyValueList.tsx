type Item = {
  label: string;
  value: string | number | boolean;
};

type KeyValueListProps = {
  items: Item[];
};

export function KeyValueList({ items }: KeyValueListProps) {
  return (
    <dl className="key-value-list">
      {items.map((item) => (
        <div key={item.label}>
          <dt>{item.label}</dt>
          <dd>{String(item.value)}</dd>
        </div>
      ))}
    </dl>
  );
}
